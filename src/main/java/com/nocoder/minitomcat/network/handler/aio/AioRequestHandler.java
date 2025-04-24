package com.nocoder.minitomcat.network.handler.aio;


import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.exception.FilterNotFoundException;
import com.nocoder.minitomcat.exception.ServletNotFoundException;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.network.handler.AbstractRequestHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.network.wrapper.aio.AioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.resource.ResourceHandler;
import com.nocoder.minitomcat.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

/**
 * Servlet运行容器
 */
@Slf4j
public class AioRequestHandler extends AbstractRequestHandler {
    private final Logger logger = LoggerFactory.getLogger(AioRequestHandler.class);

    private CompletionHandler readHandler;
    
    public AioRequestHandler(SocketWrapper socketWrapper, ServletContext servletContext, ExceptionHandler exceptionHandler, ResourceHandler resourceHandler, CompletionHandler readHandler, Request request, Response response) throws ServletNotFoundException, FilterNotFoundException {
        super(socketWrapper, servletContext, exceptionHandler, resourceHandler,request,response);
        this.readHandler = readHandler;
    }

    public CompletionHandler getReadHandler() {
        return readHandler;
    }

    public void setReadHandler(CompletionHandler readHandler) {
        this.readHandler = readHandler;
    }

    /**
     * 写回后重新调用readHandler，进行读取（猜测AIO也是保活的）
     */
    @Override
    public void flushResponse() {
        isFinished = true;
        ByteBuffer[] responseData = response.getResponseByteBuffer();
        AioSocketWrapper aioSocketWrapper = (AioSocketWrapper) socketWrapper;
        AsynchronousSocketChannel socketChannel = aioSocketWrapper.getSocketChannel();
        socketChannel.write(responseData, 0, 2, 0L, TimeUnit.MILLISECONDS, null, new CompletionHandler<Long, Object>() {

            @Override
            public void completed(Long result, Object attachment) {
                logger.info("写入完毕...");
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                socketChannel.read(byteBuffer, byteBuffer, readHandler);
            }

            @Override
            public void failed(Throwable e, Object attachment) {
                logger.info("写入失败...");
                e.printStackTrace();
            }
        });
        WebApplication.getServletContext().afterRequestDestroyed(request);
    }
}
