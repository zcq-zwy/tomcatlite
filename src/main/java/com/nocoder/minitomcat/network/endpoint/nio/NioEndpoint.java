package com.nocoder.minitomcat.network.endpoint.nio;

import com.nocoder.minitomcat.constant.NetWorkConstant;
import com.nocoder.minitomcat.network.connector.nio.IdleConnectionCleaner;
import com.nocoder.minitomcat.network.connector.nio.NioAcceptor;
import com.nocoder.minitomcat.network.connector.nio.NioPoller;
import com.nocoder.minitomcat.network.dispatcher.nio.NioDispatcher;
import com.nocoder.minitomcat.network.endpoint.Endpoint;
import com.nocoder.minitomcat.network.wrapper.nio.NioSocketWrapper;
import java.util.concurrent.ThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 29282
 */
@Slf4j
public class NioEndpoint extends Endpoint {

  private final Logger logger = LoggerFactory.getLogger(NioEndpoint.class);

  /**
   * 1. 创建 Acceptor 线程工厂（固定名称）
   */
  ThreadFactory acceptorFactory =
      DefaultThreadFactory.createWithFixedName("NioAcceptor", false, Thread.NORM_PRIORITY);

  /**
   * 2. 创建 Poller 线程工厂（带编号前缀）
   */
  ThreadFactory pollerFactory =
      DefaultThreadFactory.createWithNumberedPrefix("NioPoller", false, Thread.NORM_PRIORITY);


  private final int pollerCount = Math.min(2, Runtime.getRuntime().availableProcessors());


  private ServerSocketChannel server;
  private NioDispatcher nioDispatcher;
  private volatile boolean isRunning = true;
  private List<NioPoller> nioPollers;
  /**
   * poller轮询器
   */
  private final AtomicInteger pollerRotate = new AtomicInteger(0);


  private IdleConnectionCleaner cleaner;

  /********************************初始化**************************************************************/
  private void initDispatcherServlet() {
    nioDispatcher = new NioDispatcher();
  }

  private void initServerSocket(int port) throws IOException {
    server = ServerSocketChannel.open();
    server.bind(new InetSocketAddress(port));
    server.configureBlocking(true);
  }

  private void initPoller() throws IOException {
    nioPollers = new ArrayList<>(pollerCount);
    for (int i = 0; i < pollerCount; i++) {
      String pollName = "NioPoller-" + i;
      NioPoller nioPoller = new NioPoller(this, pollName);
      Thread pollerThread = pollerFactory.newThread(nioPoller);
      pollerThread.setDaemon(true);
      pollerThread.start();
      nioPollers.add(nioPoller);
    }
  }

  /**
   * 初始化Acceptor
   */
  private void initAcceptor() {
    NioAcceptor nioAcceptor = new NioAcceptor(this);
    Thread t = acceptorFactory.newThread(nioAcceptor);
    t.setDaemon(true);
    t.start();
  }

  /**
   * 初始化IdleSocketCleaner
   */
  private void initIdleSocketCleaner() {
    cleaner = new IdleConnectionCleaner(nioPollers);
    cleaner.start();
  }

  /**
   * ***********************初始化结束***************************************************************
   */
  @Override
  public void start(int port) {
    try {
      initDispatcherServlet();
      initServerSocket(port);
      initPoller();
      initAcceptor();
      initIdleSocketCleaner();
      logger.info("服务器启动");
    } catch (Exception e) {
      logger.error("初始化服务器失败", e);
      close();
    }
  }

  @Override
  public void close() {
    isRunning = false;
    cleaner.shutdown();
    for (NioPoller nioPoller : nioPollers) {
      try {
        nioPoller.close();
      } catch (IOException e) {
        logger.error("poller关闭失败", e);
      }
    }
    nioDispatcher.shutdown();
    try {
      server.close();
    } catch (IOException e) {
     logger.error("server关闭失败", e);
    }
  }

  /**
   * 调用dispatcher，处理这个读已就绪的客户端连接
   *
   */
  public void execute(NioSocketWrapper socketWrapper) {
    nioDispatcher.doDispatch(socketWrapper);
  }

  /**
   * 轮询Poller，实现负载均衡
   */
  private NioPoller getPoller() {

    boolean sizeIsPowerOfTwo = (nioPollers.size() & nioPollers.size() - 1) == 0;
    if (sizeIsPowerOfTwo) {
      // 位运算优化
      int next = pollerRotate.getAndIncrement();
      return nioPollers.get(next & (pollerCount - 1));
    } else {
      // 通用取模
      int size = nioPollers.size();
      int next = pollerRotate.getAndIncrement();
      int idx = (next % size + size) % size;
      return nioPollers.get(idx);
    }
  }


  public boolean isRunning() {
    return isRunning;
  }

  /**
   * 以阻塞方式来接收一个客户端的链接
   *
   */
  public SocketChannel accept() throws IOException {
    return server.accept();
  }

  /**
   * 将Acceptor接收到的socket放到轮询到的一个Poller的Queue中
   *
   */
  public void registerToPoller(SocketChannel socket) throws IOException {
//    server.configureBlocking(false);
    getPoller().register(socket, true);
//    server.configureBlocking(true);
  }

  public int getKeepAliveTimeout() {
    return NetWorkConstant.KEEP_ALIVE_TIMEOUT;
  }

}

