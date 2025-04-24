package com.nocoder.minitomcat.exception;


import com.nocoder.minitomcat.enumeration.HttpStatus;
import com.nocoder.minitomcat.exception.base.ServletException;

/**
 * 请求数据不合法
 */
public class RequestInvalidException extends ServletException {
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;
    public RequestInvalidException() {
        super(status);
    }
}
