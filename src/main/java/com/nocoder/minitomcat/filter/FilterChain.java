package com.nocoder.minitomcat.filter;

import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;

/**
 * 拦截器链
 */
public interface FilterChain {
    /**
     * 当前filter放行，由后续的filter继续进行过滤
     * @param request
     * @param response
     */
    void doFilter(Request request, Response response) ;
}
