package com.nocoder.minitomcat.network.handler.nio;

import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.exception.FilterNotFoundException;
import com.nocoder.minitomcat.exception.ServletNotFoundException;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.network.handler.AbstractRequestHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.network.wrapper.nio.NioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.resource.ResourceHandler;
import com.nocoder.minitomcat.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * 这里涉及了keep-alive的实现，如果请求头中有connection:keep-alive，
 * 就将其重新注册到Poller的Queue，等待下一次读就绪事件。
 * @author 29282
 */
@Slf4j
public class NioRequestHandler extends AbstractRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(NioRequestHandler.class);
    
    public NioRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext, ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, Request request, Response response) throws ServletNotFoundException, FilterNotFoundException {
        super(socketWrapper, servletContext, exceptionHandler, resourceHandler, request, response);
    }

    /**
     * 写入后会根据请求头Connection来判断是关闭连接还是重新将连接放回Poller，实现保活
     */
    @Override
    public void flushResponse() {
        isFinished = true;
        NioSocketWrapper nioSocketWrapper = (NioSocketWrapper) socketWrapper;
        ByteBuffer[] responseData = response.getResponseByteBuffer();
        try {
            nioSocketWrapper.getSocketChannel().write(responseData);
            List<String> connection = request.getHeaders().get("Connection");
            if (connection != null && "close".equals(connection.get(0))) {
                logger.info("CLOSE: 客户端连接{} 已关闭", nioSocketWrapper.getSocketChannel());
                nioSocketWrapper.close();
            } else {
                // keep-alive 重新注册到Poller中
                logger.info("KEEP-ALIVE: 客户端连接{} 重新注册到Poller中", nioSocketWrapper.getSocketChannel());
                nioSocketWrapper.getNioPoller().register(nioSocketWrapper.getSocketChannel(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WebApplication.getServletContext().afterRequestDestroyed(request);
    }
}
