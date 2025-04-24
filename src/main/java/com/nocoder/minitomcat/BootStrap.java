package com.nocoder.minitomcat;

import com.nocoder.minitomcat.network.endpoint.Endpoint;
import com.nocoder.minitomcat.util.PropertyUtil;

import java.util.Scanner;

public class BootStrap {

    /**
     * 服务器启动入口
     * 用户程序与服务器的接口
     */
    public static void run() {
        String port = PropertyUtil.getProperty("server.port");
        if(port == null) {
            throw new IllegalArgumentException("server.port 不存在");
        }
        String connector = PropertyUtil.getProperty("server.connector");
        if(connector == null || (!connector.equalsIgnoreCase("bio") && !connector.equalsIgnoreCase("nio") && !connector.equalsIgnoreCase("aio"))) {
            throw new IllegalArgumentException("server.network 不存在或不符合规范");
        }
        Endpoint server = Endpoint.getInstance(connector);
        server.start(Integer.parseInt(port));
        Scanner scanner = new Scanner(System.in);
        String order;
        while (scanner.hasNext()) {
            order = scanner.next();
            if ("EXIT".equals(order)) {
                server.close();
                System.exit(0);
            }
        }
    }
}
