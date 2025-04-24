package com.nocoder.minitomcat.session;

import com.nocoder.minitomcat.context.WebApplication;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 过期session的清除器
 */
@Slf4j
public class IdleSessionCleaner implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(IdleSessionCleaner.class);

    
    private ScheduledExecutorService executor;

    public IdleSessionCleaner() {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "IdleSessionCleaner");
            }
        };
        this.executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
    
    public void start() {
        executor.scheduleAtFixedRate(this, 5, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void run() {
        logger.info("开始扫描过期session...");
        WebApplication.getServletContext().cleanIdleSessions();
        logger.info("扫描结束...");
    }
}
