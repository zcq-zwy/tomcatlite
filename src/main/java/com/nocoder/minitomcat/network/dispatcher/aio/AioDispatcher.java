package com.nocoder.minitomcat.network.dispatcher.aio;

import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.network.dispatcher.AbstractDispatcher;
import com.nocoder.minitomcat.network.handler.aio.AioRequestHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.network.wrapper.aio.AioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

@Slf4j
public class AioDispatcher extends AbstractDispatcher {

    private final Logger logger = LoggerFactory.getLogger(AioDispatcher.class);
    
    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        AioSocketWrapper aioSocketWrapper = (AioSocketWrapper) socketWrapper;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        aioSocketWrapper.getSocketChannel().read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                Request request = null;
                Response response = null;
                try {
                    //解析请求
                    request = new Request(attachment.array());
                    response = new Response();
                    pool.execute(new AioRequestHandler(aioSocketWrapper, servletContext, exceptionHandler, resourceHandler, this, request, response));
                } catch (ServletException e) {
                    exceptionHandler.handle(e, response, aioSocketWrapper);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable e, ByteBuffer attachment) {
                logger.error("read failed");
                e.printStackTrace();
            }
        });
    }
}
