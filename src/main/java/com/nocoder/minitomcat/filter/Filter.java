package com.nocoder.minitomcat.filter;

import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;

/**
 * 过滤器
 * @author 29282
 */
public interface Filter {
    /**
     * 过滤器初始化
     */
    void init();

    /**
     * 过滤
     * @param request
     * @param response
     * @param filterChain
     */
    void doFilter(Request request, Response response, FilterChain filterChain) ;

    /**
     * 过滤器销毁
     */
    void destroy();
}
