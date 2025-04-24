package com.nocoder.minitomcat.network.endpoint.nio;

/**
 * <p>作者： zcq</p>
 * <p>文件名称: DefaultThreadFactory </p>
 * <p>描述: [类型描述] </p>
 * <p>创建时间: 2025/4/23 </p>
 *
 * @author <a href="mail to: 2928235428@qq.com" rel="nofollow">作者</a>
 * @version 1.0
 **/

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


/**
 * 自定义线程工厂，支持按角色动态生成线程名称（如 NioAcceptor、NioPoller-1）
 * 特性：
 * - 线程名称生成策略可定制
 * - 支持守护线程、优先级设置
 * - 内置常用生成器（固定名称、带编号前缀）
 * @author 29282
 */
public class DefaultThreadFactory implements ThreadFactory {

  private final ThreadGroup group;
  private final Function<AtomicInteger, String> nameGenerator;
  private final int priority;
  private final boolean daemon;
  private final AtomicInteger threadCounter = new AtomicInteger(1);

  /**
   * 全参数构造函数
   *
   * @param nameGenerator 名称生成函数（接收计数器，返回线程名）
   * @param daemon        是否守护线程
   * @param priority      线程优先级（1-10）
   */
  public DefaultThreadFactory(Function<AtomicInteger, String> nameGenerator,
      boolean daemon, int priority) {
    SecurityManager securityManager = System.getSecurityManager();
    this.group = (securityManager != null)
        ? securityManager.getThreadGroup()
        : Thread.currentThread().getThreadGroup();
    this.nameGenerator = nameGenerator;
    this.daemon = daemon;
    this.priority = Math.max(Thread.MIN_PRIORITY,
        Math.min(Thread.MAX_PRIORITY, priority));
  }

  /**
   * 静态工厂方法：创建带编号前缀的线程工厂（如 "NioPoller-1"）
   */
  public static DefaultThreadFactory createWithNumberedPrefix(
      String prefix, boolean daemon, int priority) {
    Function<AtomicInteger, String> nameGen = counter ->
        prefix + "-" + counter.getAndIncrement();
    return new DefaultThreadFactory(nameGen, daemon, priority);
  }

  /**
   * 静态工厂方法：创建固定名称的线程工厂（如 "NioAcceptor"）
   */
  public static DefaultThreadFactory createWithFixedName(
      String name, boolean daemon, int priority) {
    Function<AtomicInteger, String> nameGen = counter -> name;
    return new DefaultThreadFactory(nameGen, daemon, priority);
  }

  @Override
  public Thread newThread(Runnable task) {
    String threadName = nameGenerator.apply(threadCounter);
    Thread thread = new Thread(group, task, threadName);
    thread.setDaemon(daemon);
    thread.setPriority(priority);
    // 设置默认异常处理
    thread.setUncaughtExceptionHandler((t, e) -> {
      System.err.printf("Thread %s failed: %s%n", t.getName(), e.getMessage());
      e.printStackTrace();
    });
    return thread;
  }

}
