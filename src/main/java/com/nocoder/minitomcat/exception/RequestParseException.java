package com.nocoder.minitomcat.exception;

import com.nocoder.minitomcat.enumeration.HttpStatus;
import com.nocoder.minitomcat.exception.base.ServletException;

/**
 * 请求解析出国
 */
public class RequestParseException extends ServletException {
    private static final HttpStatus status = HttpStatus.BAD_REQUEST;
    public RequestParseException() {
        super(status);
    }
}
