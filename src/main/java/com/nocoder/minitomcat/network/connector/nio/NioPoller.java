package com.nocoder.minitomcat.network.connector.nio;

import com.nocoder.minitomcat.network.endpoint.nio.NioEndpoint;
import com.nocoder.minitomcat.network.wrapper.nio.NioSocketWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * 注意Poller中保存了所有的活跃Socket（成员变量sockets），其中有些socket是初次连接的，
 * 有些是keep-alive的，我还另外设置了一个IdleConnectionCleaner，用于清除一段时间内没有
 * 任何数据交换的socket，实现就是在SocketWrapper中添加一个waitBegin成员变量，在建立
 * 连接/keep-alive时设置waitBegin，并设置一个Scheduler定期扫描sockets，将当前时间
 * 距waitBegin超过阈值的连接关闭。
 * @author zcq
 */
@Slf4j
public class NioPoller implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(NioPoller.class);

    private final NioEndpoint nioEndpoint;
    @Getter
    private final Selector selector;
    private final Queue<PollerEvent> events;
    @Getter
    private final String pollerName;
    private final Map<SocketChannel, NioSocketWrapper> sockets;
    
    public NioPoller(NioEndpoint nioEndpoint, String pollerName) throws IOException {
        this.sockets = new ConcurrentHashMap<>();
        this.nioEndpoint = nioEndpoint;
        this.selector = Selector.open();
        this.events = new ConcurrentLinkedQueue<>();
        this.pollerName = pollerName;
    }

    /**
     * 注册一个新的或旧的socket至Poller中
     * 注意，只有在这里会初始化或重置waitBegin
     */
    public void register(SocketChannel socketChannel, boolean isNewSocket) {
        logger.info("Acceptor将连接到的socket放入 {} 的Queue中", pollerName);
        NioSocketWrapper wrapper;
        if (isNewSocket) {
            // 设置waitBegin
            wrapper = new NioSocketWrapper(nioEndpoint, socketChannel, this, isNewSocket);
            // 用于cleaner检测超时的socket和关闭socket
            sockets.put(socketChannel, wrapper);
        } else {
            wrapper = sockets.get(socketChannel);
            wrapper.setWorking(false);
        }
        wrapper.setWaitBegin(System.currentTimeMillis());
        events.offer(new PollerEvent(wrapper));
        // 某个线程调用select()方法后阻塞了，即使没有通道已经就绪，也有办法让其从select()方法返回。
        // 只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。
        // 阻塞在select()方法上的线程会立马返回。
        selector.wakeup();
    }

    public void close() throws IOException {
        for (NioSocketWrapper wrapper : sockets.values()) {
            wrapper.close();
        }
        events.clear();
        selector.close();
    }
    
    @Override
    public void run() {
        logger.info("{} 开始监听", Thread.currentThread().getName());
        while (nioEndpoint.isRunning()) {
            try {
                // 注册读事件
                events();
                if (selector.select() <= 0) {
                    continue;
                }
                logger.info("select()返回,开始获取当前选择器中所有注册的监听事件");
                //获取当前选择器中所有注册的监听事件
                for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    //开始监听
                    if (key.isReadable()) {
                        //如果"读取"事件已就绪
                        //交由读取事件的处理器处理
                        logger.info("serverSocket读已就绪,准备读");
                        NioSocketWrapper attachment = (NioSocketWrapper) key.attachment();
                        if (attachment != null) {
                            processSocket(attachment);
                        }
                    }
                    //处理完毕后，需要取消当前的选择键
                    it.remove();
                }
            } catch (IOException e) {
               logger.error("selector发生io错误", e);
            } catch (ClosedSelectorException e) {
                logger.info("{} 对应的selector 已关闭", this.pollerName);
            }
        }
    }

    private void processSocket(NioSocketWrapper attachment) {
        attachment.setWorking(true);
        nioEndpoint.execute(attachment);
    }

    private void events() {
        logger.info("Queue大小为{},清空Queue,将连接到的Socket注册到selector中", events.size());
        PollerEvent pollerEvent;
        for (int i = 0, size = events.size(); i < size && (pollerEvent = events.poll()) != null; i++) {
            // 回调事件
          pollerEvent.run();
        }
    }

    public void cleanTimeoutSockets() {
        for (Iterator<Map.Entry<SocketChannel, NioSocketWrapper>> it = sockets.entrySet().iterator(); it.hasNext(); ) {
            NioSocketWrapper wrapper = it.next().getValue();
            logger.info("缓存中的socket:{}", wrapper);
            if (!wrapper.getSocketChannel().isConnected()) {
                logger.info("该socket已被关闭");
                it.remove();
                continue;
            }
            if (wrapper.isWorking()) {
                logger.info("该socket正在工作中，不予关闭");
                continue;
            }
            if (System.currentTimeMillis() - wrapper.getWaitBegin() > nioEndpoint.getKeepAliveTimeout()) {
                // 反注册
                logger.info("{} keepAlive已过期", wrapper.getSocketChannel());
                try {
                    wrapper.close();
                } catch (IOException e) {
                    logger.error("关闭socket失败", e);
                }
                it.remove();
            }
        }
    }
    

    private static class PollerEvent implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(PollerEvent.class);

        private final NioSocketWrapper wrapper;

        public PollerEvent(NioSocketWrapper wrapper) {
            this.wrapper = wrapper;
        }

        @Override
        public void run() {
            logger.info("将SocketChannel的读事件注册到Poller的selector中");
            try {
                if (wrapper.getSocketChannel().isOpen()) {
                    wrapper.getSocketChannel().register(wrapper.getNioPoller().getSelector(), SelectionKey.OP_READ, wrapper);
                } else {
                    logger.error("{}已经被关闭，无法注册到Poller", wrapper.getSocketChannel());
                }
            } catch (ClosedChannelException e) {
               logger.error("socket channel已经关闭",e);
            }
        }
    }
}
