package com.nocoder.minitomcat.network.handler;

import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.exception.FilterNotFoundException;
import com.nocoder.minitomcat.exception.ServerErrorException;
import com.nocoder.minitomcat.exception.ServletNotFoundException;
import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.filter.Filter;
import com.nocoder.minitomcat.filter.FilterChain;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.resource.ResourceHandler;
import com.nocoder.minitomcat.response.Response;
import com.nocoder.minitomcat.servlet.Servlet;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;


/**
 * RequestHandler 的父类，通过父类来复用成员变量和部分方法
 * 不同IO模型的RequestHandler基本是在将Response写回客户端这部分有不同的实现，
 * 在这里被设置为了抽象方法
 */
@Slf4j
public abstract class AbstractRequestHandler implements FilterChain, Runnable {
    private final Logger logger = LoggerFactory.getLogger(AbstractQueuedSynchronizer.class);

    protected Request request;
    protected Response response;
    protected SocketWrapper socketWrapper;
    protected ServletContext servletContext;
    protected ExceptionHandler exceptionHandler;
    protected ResourceHandler resourceHandler;
    protected boolean isFinished;
    protected Servlet servlet;
    protected List<Filter> filters;
    private int filterIndex = 0;

    public AbstractRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext, ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, Request request, Response response) throws ServletNotFoundException, FilterNotFoundException {
        this.socketWrapper = socketWrapper;
        this.servletContext = servletContext;
        this.exceptionHandler = exceptionHandler;
        this.resourceHandler = resourceHandler;
        this.isFinished = false;
        this.request = request;
        this.response = response;
        request.setServletContext(servletContext);
        request.setRequestHandler(this);
        response.setRequestHandler(this);
        // 根据url查询匹配的servlet，结果是0个或1个
        servlet = servletContext.mapServlet(request.getUrl());
        // 根据url查询匹配的filter，结果是0个或多个
        filters = servletContext.mapFilter(request.getUrl());
    }

    public Logger getLogger() {
        return logger;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public SocketWrapper getSocketWrapper() {
        return socketWrapper;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public int getFilterIndex() {
        return filterIndex;
    }

    /**
     * 入口
     */
    /*
        这里有一个递归的过程。
        run方法开始执行会先调用第一个filter，第一个filter如果放行，那么会调用filterChain
        （也就是requestHandler，this）的doFilter，此时会将filterIndex++，然后调用下一个
        filter的doFilter方法，如此反复，直至所有filter都被调用，此时将调用service方法，
        执行servlet业务逻辑。如果不放行，那么会在某个filter的doFilter执行完毕后结束整个
        逻辑（一般需要进行重定向）。
     */
    @Override
    public void run() {
        // 如果没有filter，则直接执行servlet
        if (filters.isEmpty()) {
            service();
        } else {
            // 先执行filter
            doFilter(request, response);
        }
    }

    /**
     * 递归执行，自定义filter中如果同意放行，那么会调用filterChain(也就是requestHandler)的doiFilter方法，
     * 此时会执行下一个filter的doFilter方法；
     * 如果不放行，那么会在sendRedirect之后将响应数据写回客户端，结束；
     * 如果所有Filter都执行完毕，那么会调用service方法，执行servlet逻辑
     * @param request
     * @param response
     */
    @Override
    public void doFilter(Request request, Response response) {
        if (filterIndex < filters.size()) {
            filters.get(filterIndex++).doFilter(request, response, this);
        } else {
            service();
        }
    }

    /**
     * 调用servlet
     */
    private void service() {
        try {
            //处理动态资源，交由某个Servlet执行
            //Servlet是单例多线程
            //Servlet在RequestHandler中执行
            servlet.service(request, response);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        } catch (Exception e) {
            //其他未知异常
            e.printStackTrace();
            exceptionHandler.handle(new ServerErrorException(), response, socketWrapper);
        } finally {
            if (!isFinished) {
                flushResponse();
            }
        }
        logger.info("请求处理完毕");
    }

    /**
     * 响应数据写回到客户端
     */
    public abstract void flushResponse();
}
