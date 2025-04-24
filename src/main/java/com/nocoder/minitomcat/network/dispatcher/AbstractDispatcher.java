package com.nocoder.minitomcat.network.dispatcher;

import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.resource.ResourceHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 所有Dispatcher（请求分发器）的父类
 * 请求分发线程，往往对应一个线程池，负责读取Request以及将RequestHandler放入线程池中执行
 */
public abstract class AbstractDispatcher {
    protected ResourceHandler resourceHandler;
    protected ExceptionHandler exceptionHandler;
    protected ThreadPoolExecutor pool;
    protected ServletContext servletContext;
    
    public AbstractDispatcher() {
        this.servletContext = WebApplication.getServletContext();
        this.exceptionHandler = new ExceptionHandler();
        this.resourceHandler = new ResourceHandler(exceptionHandler);
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Worker Pool-" + count++);
            }
        };
        this.pool = new ThreadPoolExecutor(100, 100, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(200), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    /**
     * 关闭
     */
    public void shutdown() {
        pool.shutdown();
        servletContext.destroy();
    }

    /**
     * 分发请求
     * @param socketWrapper
     */
    public abstract void doDispatch(SocketWrapper socketWrapper);
}
