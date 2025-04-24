package com.nocoder.minitomcat.exception;


import com.nocoder.minitomcat.enumeration.HttpStatus;
import com.nocoder.minitomcat.exception.base.ServletException;

/**
 * 静态资源未找到
 */
public class ResourceNotFoundException extends ServletException {
    private static final HttpStatus status = HttpStatus.NOT_FOUND;
    public ResourceNotFoundException() {
        super(status);
    }
}
