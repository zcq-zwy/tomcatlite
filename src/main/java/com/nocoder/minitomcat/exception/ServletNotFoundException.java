package com.nocoder.minitomcat.exception;


import com.nocoder.minitomcat.enumeration.HttpStatus;
import com.nocoder.minitomcat.exception.base.ServletException;

/**
 * 未找到对应的Servlet（web.xml配置错误）
 */
public class ServletNotFoundException extends ServletException {
    private static final HttpStatus status = HttpStatus.NOT_FOUND;
    public ServletNotFoundException() {
        super(status);
    }
}
