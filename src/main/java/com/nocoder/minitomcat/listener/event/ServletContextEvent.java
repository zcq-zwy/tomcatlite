package com.nocoder.minitomcat.listener.event;


import com.nocoder.minitomcat.context.ServletContext;

/**
 * servletContext相关的事件
 */
public class ServletContextEvent extends java.util.EventObject { 

 
    public ServletContextEvent(ServletContext source) {
        super(source);
    }
    
    public ServletContext getServletContext () {
        return (ServletContext) super.getSource();
    }
}

