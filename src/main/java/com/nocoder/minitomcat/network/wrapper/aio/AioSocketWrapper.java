package com.nocoder.minitomcat.network.wrapper.aio;

import com.nocoder.minitomcat.network.endpoint.aio.AioEndpoint;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

@Slf4j
public class AioSocketWrapper implements SocketWrapper {
    private AioEndpoint server;
    private AsynchronousSocketChannel socketChannel;
    private volatile long waitBegin;
    private volatile boolean isWorking;
    
    public AioSocketWrapper(AioEndpoint server, AsynchronousSocketChannel socketChannel) {
        this.server = server;
        this.socketChannel = socketChannel;
        this.isWorking = false;
    }
    
    public void close() throws IOException {
        socketChannel.close();
    }

    public AioEndpoint getServer() {
        return server;
    }

    public void setServer(AioEndpoint server) {
        this.server = server;
    }

    public AsynchronousSocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public long getWaitBegin() {
        return waitBegin;
    }

    public void setWaitBegin(long waitBegin) {
        this.waitBegin = waitBegin;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    @Override
    public String toString() {
        return socketChannel.toString();
    }
}
