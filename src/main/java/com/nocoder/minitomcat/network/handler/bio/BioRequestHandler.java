package com.nocoder.minitomcat.network.handler.bio;

import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.exception.FilterNotFoundException;
import com.nocoder.minitomcat.exception.ServletNotFoundException;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.network.handler.AbstractRequestHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.network.wrapper.bio.BioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.resource.ResourceHandler;
import com.nocoder.minitomcat.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Servlet运行容器
 */

/*
    这里就是把response转为byte数组，然后写回即可。
    注意这里在写回之后会将连接立刻关闭，对应着HTTP的connection:close，也就是HTTP短连接。
    在NIO中实现了HTTP持久连接，也就是connection:keep-alive。在后面的基于JMeter压测中，
    在测试BIO时注意要设置请求头Connection为close，否则会出现大量的异常（connection refused）。
 */
@Slf4j
public class BioRequestHandler extends AbstractRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(BioRequestHandler.class);

    public BioRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext, ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, Request request, Response response) throws ServletNotFoundException, FilterNotFoundException {
        super(socketWrapper, servletContext, exceptionHandler, resourceHandler, request, response);
    }



    /**
     * 写回后立即关闭socket
     */
    @Override
    public void flushResponse() {
        isFinished = true;
        BioSocketWrapper bioSocketWrapper = (BioSocketWrapper) socketWrapper;
        byte[] bytes = response.getResponseBytes();
        OutputStream os = null;
        try {
            os = bioSocketWrapper.getSocket().getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("socket closed");
        } finally {
            try {
                os.close();
                bioSocketWrapper.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WebApplication.getServletContext().afterRequestDestroyed(request);
    }
}
