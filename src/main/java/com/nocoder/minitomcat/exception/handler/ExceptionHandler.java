package com.nocoder.minitomcat.exception.handler;

import com.nocoder.minitomcat.exception.RequestInvalidException;
import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.network.wrapper.SocketWrapper;
import com.nocoder.minitomcat.response.Header;
import com.nocoder.minitomcat.response.Response;
import com.nocoder.minitomcat.util.IOUtil;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.nocoder.minitomcat.constant.ContextConstant.ERROR_PAGE;

/**
 * 异常处理器
 * 会根据异常对应的HTTP Status设置response的状态以及相应的错误页面
 */
@Slf4j
public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public void handle(ServletException e, Response response, SocketWrapper socketWrapper) {
        try {
            if (e instanceof RequestInvalidException) {
                logger.info("请求无法读取，丢弃");
                socketWrapper.close();
            } else {
                logger.info("抛出异常:{}", e.getClass().getName());
                e.printStackTrace();
                response.addHeader(new Header("Connection", "close"));
                response.setStatus(e.getStatus());
                response.setBody(IOUtil.getBytesFromFile(
                        String.format(ERROR_PAGE, String.valueOf(e.getStatus().getCode()))));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
