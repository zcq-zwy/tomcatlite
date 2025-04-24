package com.nocoder.minitomcat.network.connector.aio;

import com.nocoder.minitomcat.network.endpoint.aio.AioEndpoint;
import com.nocoder.minitomcat.network.wrapper.aio.AioSocketWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

@Slf4j
public class AioAcceptor implements CompletionHandler<AsynchronousSocketChannel, Void> {
    private final Logger logger = LoggerFactory.getLogger(AioAcceptor.class);

    private AioEndpoint aioEndpoint;

    public AioAcceptor(AioEndpoint aioEndpoint) {
        this.aioEndpoint = aioEndpoint;
    }

    @Override
    public void completed(AsynchronousSocketChannel client, Void attachment) {
        aioEndpoint.accept();
        aioEndpoint.execute(new AioSocketWrapper(aioEndpoint, client));
    }

    @Override
    public void failed(Throwable e, Void attachment) {
        logger.info("accept failed...");
        e.printStackTrace();
    }
}
