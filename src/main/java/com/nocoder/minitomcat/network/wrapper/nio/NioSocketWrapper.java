package com.nocoder.minitomcat.network.wrapper.nio;

import com.nocoder.minitomcat.network.connector.nio.NioPoller;
import com.nocoder.minitomcat.network.endpoint.nio.NioEndpoint;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
public class NioSocketWrapper implements SocketWrapper {
    private final NioEndpoint server;
    private final SocketChannel socketChannel;
    private final NioPoller nioPoller;
    private final boolean isNewSocket;
    private volatile long waitBegin;
    private volatile boolean isWorking;
    
    public NioSocketWrapper(NioEndpoint server, SocketChannel socketChannel, NioPoller nioPoller, boolean isNewSocket) {
        this.server = server;
        this.socketChannel = socketChannel;
        this.nioPoller = nioPoller;
        this.isNewSocket = isNewSocket;
        this.isWorking = false;
    }
    
    public void close() throws IOException {
        socketChannel.keyFor(nioPoller.getSelector()).cancel();
        socketChannel.close();
    }

    public NioEndpoint getServer() {
        return server;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public NioPoller getNioPoller() {
        return nioPoller;
    }

    public boolean isNewSocket() {
        return isNewSocket;
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
