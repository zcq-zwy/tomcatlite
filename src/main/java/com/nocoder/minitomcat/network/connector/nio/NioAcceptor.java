package com.nocoder.minitomcat.network.connector.nio;

import com.nocoder.minitomcat.network.endpoint.nio.NioEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Nio 请求接收器
 * @author 29282
 */
@Slf4j
public class NioAcceptor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(NioAcceptor.class);

    private final NioEndpoint nioEndpoint;
    
    public NioAcceptor(NioEndpoint nioEndpoint) {
        this.nioEndpoint = nioEndpoint;
    }
    
    @Override
    public void run() {
        logger.info("{} 开始监听",Thread.currentThread().getName());
        while (nioEndpoint.isRunning()) {
            SocketChannel client;
            try {
                client = nioEndpoint.accept();
                if(client == null){
                    continue;
                }
                client.configureBlocking(false);
                logger.info("Acceptor接收到连接请求 {}",client);
                nioEndpoint.registerToPoller(client); 
                logger.info("socketWrapper:{}", client);
            } catch (IOException e) {
                logger.error("Acceptor接收连接失败", e);
            }
        }
    }
}
