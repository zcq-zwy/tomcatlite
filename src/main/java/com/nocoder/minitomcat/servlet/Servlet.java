package com.nocoder.minitomcat.servlet;

import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;

import java.io.IOException;

/**
 * @author 29282
 */
public interface Servlet {
    void init();

    void destroy();

    void service(Request request, Response response) throws ServletException, IOException;
}
