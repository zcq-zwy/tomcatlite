package com.nocoder.minitomcat.network.connector.nio;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author 29282
 */
@Slf4j
public class IdleConnectionCleaner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(IdleConnectionCleaner.class);

    private ScheduledExecutorService executor;
    private List<NioPoller> nioPollers;

    public IdleConnectionCleaner(List<NioPoller> nioPollers) {
        this.nioPollers = nioPollers;
    }

    public void start() {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "IdleConnectionCleaner");
            }
        };
        executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void run() {
        for (NioPoller nioPoller : nioPollers) {
            logger.info("Cleaner 检测{} 所持有的Socket中...", nioPoller.getPollerName());
            nioPoller.cleanTimeoutSockets();
        }
        logger.info("检测结束...");
    }
}
