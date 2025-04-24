package com.nocoder.minitomcat.network.endpoint.bio;

import com.nocoder.minitomcat.network.connector.bio.BioAcceptor;
import com.nocoder.minitomcat.network.dispatcher.bio.BioDispatcher;
import com.nocoder.minitomcat.network.endpoint.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO网络传输模块的入口
 */
@Slf4j
public class BioEndpoint extends Endpoint {
    private final Logger logger = LoggerFactory.getLogger(BioEndpoint.class);

    private ServerSocket server;
    private BioAcceptor acceptor;
    private BioDispatcher dispatcher;
    private volatile boolean isRunning = true;
    
    @Override
    public void start(int port) {
        try {
            dispatcher = new BioDispatcher();
            server = new ServerSocket(port);
            initAcceptor();
            logger.info("服务器启动");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("初始化服务器失败");
            close();
        }
    }
    
    private void initAcceptor() {
        acceptor = new BioAcceptor(this, dispatcher);
        Thread t = new Thread(acceptor, "bio-acceptor");
        // 将请求接收线程acceptor设置为守护线程
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void close() {
        isRunning = false;
        dispatcher.shutdown();
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket accept() throws IOException {
        return server.accept();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
