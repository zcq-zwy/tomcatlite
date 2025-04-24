package com.nocoder.minitomcat.network.wrapper.bio;

import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;

/**
 * Socket的包装类
 */
@Slf4j
public class BioSocketWrapper implements SocketWrapper {
    private Socket socket;
    public BioSocketWrapper(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

}
