package com.nocoder.minitomcat.network.connector.bio;

import com.nocoder.minitomcat.network.dispatcher.bio.BioDispatcher;
import com.nocoder.minitomcat.network.endpoint.bio.BioEndpoint;
import com.nocoder.minitomcat.network.wrapper.bio.BioSocketWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * 请求接收线程，这是一个实现Runnable接口的线程类
 * @author 29282
 */
@Slf4j
public class BioAcceptor implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(BioAcceptor.class);

    private BioEndpoint server;
    private BioDispatcher dispatcher;
    
    public BioAcceptor(BioEndpoint server,BioDispatcher dispatcher) {
        this.server = server;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        logger.info("开始监听");
        while (server.isRunning()) {
            Socket client;
            try {
                //TCP的短连接，请求处理完即关闭
                client = server.accept();
                logger.info("client:{}", client);
                dispatcher.doDispatch(new BioSocketWrapper(client));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
