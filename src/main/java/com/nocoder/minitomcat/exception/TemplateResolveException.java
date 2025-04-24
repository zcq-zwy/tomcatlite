package com.nocoder.minitomcat.exception;


import com.nocoder.minitomcat.enumeration.HttpStatus;
import com.nocoder.minitomcat.exception.base.ServletException;

/**
 * 模板引擎解析错误（html文件编写错误）
 */
public class TemplateResolveException extends ServletException {
    private static final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    public TemplateResolveException() {
        super(status);
    }
}
   
