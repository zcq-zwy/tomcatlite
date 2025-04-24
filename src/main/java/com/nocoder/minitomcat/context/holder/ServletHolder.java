package com.nocoder.minitomcat.context.holder;

import com.nocoder.minitomcat.servlet.Servlet;

/**
 * @author 29282
 */
public class ServletHolder {
    private Servlet servlet;
    private String servletClass;

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public ServletHolder(String servletClass) {
        this.servletClass = servletClass;
    }
}
