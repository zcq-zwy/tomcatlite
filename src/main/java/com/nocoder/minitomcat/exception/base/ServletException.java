package com.nocoder.minitomcat.exception.base;

import com.nocoder.minitomcat.enumeration.HttpStatus;

/**
 * 根异常
 */
public class ServletException extends Exception {
    private HttpStatus status;
    public ServletException(HttpStatus status){
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
