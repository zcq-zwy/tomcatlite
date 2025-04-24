package com.nocoder.minitomcat.context;

import com.nocoder.minitomcat.context.holder.FilterHolder;
import com.nocoder.minitomcat.context.holder.ServletHolder;

import com.nocoder.minitomcat.cookie.Cookie;
import com.nocoder.minitomcat.exception.FilterNotFoundException;
import com.nocoder.minitomcat.exception.ServletNotFoundException;
import com.nocoder.minitomcat.filter.Filter;
import com.nocoder.minitomcat.listener.HttpSessionListener;
import com.nocoder.minitomcat.listener.ServletContextListener;
import com.nocoder.minitomcat.listener.ServletRequestListener;
import com.nocoder.minitomcat.listener.event.HttpSessionEvent;
import com.nocoder.minitomcat.listener.event.ServletContextEvent;
import com.nocoder.minitomcat.listener.event.ServletRequestEvent;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;
import com.nocoder.minitomcat.servlet.Servlet;
import com.nocoder.minitomcat.session.HttpSession;
import com.nocoder.minitomcat.session.IdleSessionCleaner;

import com.nocoder.minitomcat.util.UUIDUtil;
import com.nocoder.minitomcat.util.XMLUtil;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.util.AntPathMatcher;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.nocoder.minitomcat.constant.ContextConstant.DEFAULT_SERVLET_ALIAS;
import static com.nocoder.minitomcat.constant.ContextConstant.DEFAULT_SESSION_EXPIRE_TIME;


/**
 * ServletContext，在应用启动时被初始化
 * @author 29282
 */
@Slf4j
public class ServletContext {
    /**
     * 别名->类名
     * 一个Servlet类只能有一个Servlet别名，一个Servlet别名只能对应一个Servlet类
     */
    private Map<String, ServletHolder> servlets;
    /**
     * 一个Servlet可以对应多个URL，一个URL只能对应一个Servlet
     * URL Pattern -> Servlet别名
     */
    private Map<String, String> servletMapping;


    /**
     * 别名->类名
     */
    private Map<String, FilterHolder> filters;
    /**
     * URL Pattern -> 别名列表，注意同一个URLPattern可以对应多个Filter，但只能对应一个Servlet
     */
    private Map<String, List<String>> filterMapping;

    /**
     * 监听器们
     */
    private List<ServletContextListener> servletContextListeners;
    private List<HttpSessionListener> httpSessionListeners;
    private List<ServletRequestListener> servletRequestListeners;

    /**
     * 域
     */
    private Map<String, Object> attributes;
    /**
     * 整个应用对应的session们
     */
    private Map<String, HttpSession> sessions;
    /**
     * 路径匹配器，由Spring提供
     */
    private AntPathMatcher matcher;

    private IdleSessionCleaner idleSessionCleaner;


    public ServletContext() throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        init();
    }

    /**
     * 由URL得到对应的一个Servlet实例
     *
     * @param url
     * @return
     * @throws ServletNotFoundException
     */
    public Servlet mapServlet(String url) throws ServletNotFoundException {
        // 1、精确匹配

        String servletAlias = servletMapping.get(url);
        if (servletAlias != null) {
            return initAndGetServlet(servletAlias);
        }
        // 2、路径匹配
        List<String> matchingPatterns = new ArrayList<>();
        Set<String> patterns = servletMapping.keySet();
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                matchingPatterns.add(pattern);
            }
        }

        //把路径最长得取出来
        if (!matchingPatterns.isEmpty()) {
            Comparator<String> patternComparator = matcher.getPatternComparator(url);
            Collections.sort(matchingPatterns, patternComparator);
            String bestMatch = matchingPatterns.get(0);
            return initAndGetServlet(bestMatch);
        }
        return initAndGetServlet(DEFAULT_SERVLET_ALIAS);
    }

    /**
     * 初始化并获取Servlet实例，如果已经初始化过则直接返回
     *
     * @param servletAlias
     * @return
     * @throws ServletNotFoundException
     */
    private Servlet initAndGetServlet(String servletAlias) throws ServletNotFoundException {
        ServletHolder servletHolder = servlets.get(servletAlias);
        if (servletHolder == null) {
            throw new ServletNotFoundException();
        }
        if (servletHolder.getServlet() == null) {
            try {
                Servlet servlet = (Servlet) Class.forName(servletHolder.getServletClass()).newInstance();
                servlet.init();
                servletHolder.setServlet(servlet);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return servletHolder.getServlet();
    }


    /**
     * 由URL得到一系列匹配的Filter实例
     *
     * @param url
     * @return
     */
    public List<Filter> mapFilter(String url) throws FilterNotFoundException {
        List<String> matchingPatterns = new ArrayList<>();
        Set<String> patterns = filterMapping.keySet();
        for (String pattern : patterns) {
            if (matcher.match(pattern, url)) {
                matchingPatterns.add(pattern);
            }
        }

        Set<String> filterAliases = matchingPatterns.stream().flatMap(pattern -> this.filterMapping.get(pattern).stream()).collect(Collectors.toSet());
        List<Filter> result = new ArrayList<>();
        for (String alias : filterAliases) {
            result.add(initAndGetFilter(alias));
        }
        return result;
    }

    /**
     * 初始化并返回Filter实例，如果已经初始化过则直接返回
     *
     * @param filterAlias
     * @return
     * @throws FilterNotFoundException
     */
    private Filter initAndGetFilter(String filterAlias) throws FilterNotFoundException {
        FilterHolder filterHolder = filters.get(filterAlias);
        if (filterHolder == null) {
            throw new FilterNotFoundException();
        }
        if (filterHolder.getFilter() == null) {
            try {
                Filter filter = (Filter) Class.forName(filterHolder.getFilterClass()).newInstance();
                filter.init();
                filterHolder.setFilter(filter);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return filterHolder.getFilter();
    }

    /**
     * 应用初始化
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public void init() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        this.servlets = new ConcurrentHashMap<>();
        this.servletMapping = new HashMap<>();
        this.attributes = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
        this.filters = new HashMap<>();
        this.filterMapping = new HashMap<>();
        this.matcher = new AntPathMatcher();
        this.idleSessionCleaner = new IdleSessionCleaner();
        this.idleSessionCleaner.start();
        this.servletContextListeners = new ArrayList<>();
        this.httpSessionListeners = new ArrayList<>();
        this.servletRequestListeners = new ArrayList<>();
        parseConfig();
        ServletContextEvent servletContextEvent = new ServletContextEvent(this);
        for (ServletContextListener listener : servletContextListeners) {
            listener.contextInitialized(servletContextEvent);
        }
    }

    /**
     * 应用关闭前被调用
     */
    public void destroy() {
        servlets.values().forEach(servletHolder -> {
            if (servletHolder.getServlet() != null) {
                servletHolder.getServlet().destroy();
            }
        });
        filters.values().forEach(filterHolder -> {
            if (filterHolder.getFilter() != null) {
                filterHolder.getFilter().destroy();
            }
        });
        ServletContextEvent servletContextEvent = new ServletContextEvent(this);
        for (ServletContextListener listener : servletContextListeners) {
            listener.contextDestroyed(servletContextEvent);
        }
    }

    /**
     * web.xml文件解析，比如servlet，filter，listener等
     */
    private void parseConfig() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Document doc = XMLUtil.getDocument(ServletContext.class.getResourceAsStream("/web.xml"));
        Element root = doc.getRootElement();


        // 解析servlet
        List<Element> servlets = root.elements("servlet");
        for (Element servletEle : servlets) {
            String key = servletEle.element("servlet-name").getText();
            String value = servletEle.element("servlet-class").getText();
            this.servlets.put(key, new ServletHolder(value));
        }

        List<Element> servletMapping = root.elements("servlet-mapping");
        for (Element mapping : servletMapping) {
            List<Element> urlPatterns = mapping.elements("url-pattern");
            String value = mapping.element("servlet-name").getText();
            for (Element urlPattern : urlPatterns) {
                this.servletMapping.put(urlPattern.getText(), value);
            }
        }

        // 解析 filter
        List<Element> filters = root.elements("filter");
        for (Element filterEle : filters) {
            String key = filterEle.element("filter-name").getText();
            String value = filterEle.element("filter-class").getText();
            this.filters.put(key, new FilterHolder(value));
        }

        List<Element> filterMapping = root.elements("filter-mapping");
        for (Element mapping : filterMapping) {
            List<Element> urlPatterns = mapping.elements("url-pattern");
            String value = mapping.element("filter-name").getText();
            for (Element urlPattern : urlPatterns) {
                List<String> values = this.filterMapping.get(urlPattern.getText());
                if (values == null) {
                    values = new ArrayList<>();
                    this.filterMapping.put(urlPattern.getText(), values);
                }
                values.add(value);
            }
        }

        // 解析listener
        Element listener = root.element("listener");
        List<Element> listenerEles = listener.elements("listener-class");
        for (Element listenerEle : listenerEles) {
            EventListener eventListener = (EventListener) Class.forName(listenerEle.getText()).newInstance();
            if (eventListener instanceof ServletContextListener) {
                servletContextListeners.add((ServletContextListener) eventListener);
            }
            if (eventListener instanceof HttpSessionListener) {
                httpSessionListeners.add((HttpSessionListener) eventListener);
            }
            if (eventListener instanceof ServletRequestListener) {
                servletRequestListeners.add((ServletRequestListener) eventListener);
            }
        }
    }

    /**
     * 获取session
     * @param JSESSIONID
     * @return
     */
    public HttpSession getSession(String JSESSIONID) {
        return sessions.get(JSESSIONID);
    }

    /**
     * 创建session
     * @param response
     * @return
     */
    public HttpSession createSession(Response response) {
        HttpSession session = new HttpSession(UUIDUtil.uuid());
        sessions.put(session.getId(), session);
        response.addCookie(new Cookie("JSESSIONID", session.getId()));
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent(session);
        for (HttpSessionListener listener : httpSessionListeners) {
            listener.sessionCreated(httpSessionEvent);
        }
        return session;
    }

    /**
     * 销毁session
     * @param session
     */
    public void invalidateSession(HttpSession session) {
        sessions.remove(session.getId());
        afterSessionDestroyed(session);
    }

    /**
     * 清除空闲的session
     * 由于ConcurrentHashMap是线程安全的，所以remove不需要进行加锁
     */
    public void cleanIdleSessions() {
        for (Iterator<Map.Entry<String, HttpSession>> it = sessions.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, HttpSession> entry = it.next();
            if (Duration.between(entry.getValue().getLastAccessed(), Instant.now()).getSeconds() >= DEFAULT_SESSION_EXPIRE_TIME) {
                 log.info("该session {} 已过期", entry.getKey());
                afterSessionDestroyed(entry.getValue());
                it.remove();
            }
        }
    }
    
    private void afterSessionDestroyed(HttpSession session) {
        HttpSessionEvent httpSessionEvent = new HttpSessionEvent(session);
        for (HttpSessionListener listener : httpSessionListeners) {
            listener.sessionDestroyed(httpSessionEvent);
        }
    }

    public void afterRequestCreated(Request request) {
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(this, request);
        for (ServletRequestListener listener : servletRequestListeners) {
            listener.requestInitialized(servletRequestEvent);
        }
    }

    public void afterRequestDestroyed(Request request) {
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(this, request);
        for (ServletRequestListener listener : servletRequestListeners) {
            listener.requestDestroyed(servletRequestEvent);
        }
    }

        public Object getAttribute(String key) {
        return attributes.get(key);
    }



//    @Override
//    public String getContextPath() {
//        return null;
//    }
//
//    @Override
//    public javax.servlet.ServletContext getContext(String uripath) {
//        return null;
//    }
//
//    @Override
//    public int getMajorVersion() {
//        return 0;
//    }
//
//    @Override
//    public int getMinorVersion() {
//        return 0;
//    }
//
//    @Override
//    public int getEffectiveMajorVersion() {
//        return 0;
//    }
//
//    @Override
//    public int getEffectiveMinorVersion() {
//        return 0;
//    }
//
//    @Override
//    public String getMimeType(String file) {
//        return null;
//    }
//
//    @Override
//    public Set<String> getResourcePaths(String path) {
//        return null;
//    }
//
//    @Override
//    public URL getResource(String path) throws MalformedURLException {
//        return null;
//    }
//
//    @Override
//    public InputStream getResourceAsStream(String path) {
//        return null;
//    }
//
//    @Override
//    public RequestDispatcher getRequestDispatcher(String path) {
//        return null;
//    }
//
//    @Override
//    public RequestDispatcher getNamedDispatcher(String name) {
//        return null;
//    }
//
//    @Override
//    public javax.servlet.Servlet getServlet(String name) throws ServletException {
//        return null;
//    }
//
//    @Override
//    public Enumeration<javax.servlet.Servlet> getServlets() {
//        return null;
//    }
//
//    @Override
//    public Enumeration<String> getServletNames() {
//        return null;
//    }
//
//    @Override
//    public void log(String msg) {
//
//    }
//
//    @Override
//    public void log(Exception exception, String msg) {
//
//    }
//
//    @Override
//    public void log(String message, Throwable throwable) {
//
//    }
//
//    @Override
//    public String getRealPath(String path) {
//        return null;
//    }
//
//    @Override
//    public String getServerInfo() {
//        return null;
//    }
//
//    @Override
//    public String getInitParameter(String name) {
//        return null;
//    }
//
//    @Override
//    public Enumeration<String> getInitParameterNames() {
//        return null;
//    }
//
//    @Override
//    public boolean setInitParameter(String name, String value) {
//        return false;
//    }
//
//    @Override
//    public Object getAttribute(String key) {
//        return attributes.get(key);
//    }
//
//    @Override
//    public Enumeration<String> getAttributeNames() {
//        return null;
//    }
//
//    @Override
//    public void setAttribute(String key, Object value) {
//        attributes.put(key, value);
//    }
//
//    @Override
//    public void removeAttribute(String name) {
//
//    }
//
//    @Override
//    public String getServletContextName() {
//        return null;
//    }
//
//    @Override
//    public Dynamic addServlet(String servletName, String className) {
//        return null;
//    }
//
//    @Override
//    public Dynamic addServlet(String servletName, javax.servlet.Servlet servlet) {
//        return null;
//    }
//
//    @Override
//    public Dynamic addServlet(String servletName,
//        Class<? extends javax.servlet.Servlet> servletClass) {
//        return null;
//    }
//
//
//    @Override
//    public <T extends javax.servlet.Servlet> T createServlet(Class<T> clazz)
//        throws ServletException {
//        return null;
//    }
//
//    @Override
//    public ServletRegistration getServletRegistration(String servletName) {
//        return null;
//    }
//
//    @Override
//    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
//        return null;
//    }
//
//    @Override
//    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
//        return null;
//    }
//
//    @Override
//    public FilterRegistration.Dynamic addFilter(String filterName, javax.servlet.Filter filter) {
//        return null;
//    }
//
//    @Override
//    public FilterRegistration.Dynamic addFilter(String filterName,
//        Class<? extends javax.servlet.Filter> filterClass) {
//        return null;
//    }
//
//    @Override
//    public <T extends javax.servlet.Filter> T createFilter(Class<T> clazz) throws ServletException {
//        return null;
//    }
//
//    @Override
//    public FilterRegistration getFilterRegistration(String filterName) {
//        return null;
//    }
//
//    @Override
//    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
//        return null;
//    }
//
//    @Override
//    public SessionCookieConfig getSessionCookieConfig() {
//        return null;
//    }
//
//    @Override
//    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
//
//    }
//
//    @Override
//    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
//        return null;
//    }
//
//    @Override
//    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
//        return null;
//    }
//
//    @Override
//    public void addListener(String className) {
//
//    }
//
//    @Override
//    public <T extends EventListener> void addListener(T t) {
//
//    }
//
//    @Override
//    public void addListener(Class<? extends EventListener> listenerClass) {
//
//    }
//
//    @Override
//    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
//        return null;
//    }
//
//    @Override
//    public JspConfigDescriptor getJspConfigDescriptor() {
//        return null;
//    }
//
//    @Override
//    public ClassLoader getClassLoader() {
//        return null;
//    }
//
//    @Override
//    public void declareRoles(String... roleNames) {
//
//    }
//
//    @Override
//    public String getVirtualServerName() {
//        return null;
//    }




}
