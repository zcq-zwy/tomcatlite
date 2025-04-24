package com.nocoder.minitomcat.network.dispatcher.nio;

import com.nocoder.minitomcat.exception.ServerErrorException;
import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.network.dispatcher.AbstractDispatcher;
import com.nocoder.minitomcat.network.handler.nio.NioRequestHandler;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.network.wrapper.nio.NioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author 29282
 */
@Slf4j
public class NioDispatcher extends AbstractDispatcher {
    private final Logger logger = LoggerFactory.getLogger(NioDispatcher.class);

    /**
     * 分发请求，注意IO读取必须放在IO线程中进行，不能放到线程池中，否则会出现多个线程同时读同一个socket数据的情况
     * 1、读取数据
     * 2、构造request，response
     * 3、将业务放入到线程池中处理
     */
    @Override
    public void doDispatch(SocketWrapper socketWrapper) {
        NioSocketWrapper nioSocketWrapper = (NioSocketWrapper) socketWrapper;
        logger.info("已经将请求放入worker线程池中");
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        logger.info("开始读取Request");
        Request request;
        Response response = null;
        try {
            ByteArrayOutputStream requestData = new ByteArrayOutputStream();
            while (nioSocketWrapper.getSocketChannel().read(buffer) > 0) {
                buffer.flip();
                requestData.write(buffer.array());
            }
            requestData.close();
            request = new Request(requestData.toByteArray());
            response = new Response();
            pool.execute(new NioRequestHandler(nioSocketWrapper, servletContext, exceptionHandler, resourceHandler, request, response));
        } catch (IOException e) {
            e.printStackTrace();
            exceptionHandler.handle(new ServerErrorException(), response, nioSocketWrapper);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, nioSocketWrapper);
        }
    }
}
