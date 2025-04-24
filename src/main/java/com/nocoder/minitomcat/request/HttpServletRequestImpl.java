package com.nocoder.minitomcat.request;

import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.util.PropertyUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

/**
 * <p>作者： zcq</p>
 * <p>文件名称: HttpServletRequestImpl </p>
 * <p>描述: [类型描述] </p>
 * <p>创建时间: 2025/4/23 </p>
 *
 * @author <a href="mail to: 2928235428@qq.com" rel="nofollow">作者</a>
 * @version 1.0
 **/
public class HttpServletRequestImpl implements HttpServletRequest {

  private final Request exchangeRequest;

  private  volatile  boolean inputCalled;

//  private final ServletContext servletContext = WebApplication.getServletContext();

  public HttpServletRequestImpl( Request request) {
    this.exchangeRequest = request;
  }

  @Override
  public String getAuthType() {
    return null;
  }

  @Override
  public Cookie[] getCookies() {
//
    return null;
  }

  @Override
  public long getDateHeader(String s) {
    return 0;
  }

  @Override
  public String getHeader(String s) {
    return exchangeRequest.getHeaders().get(s).get(0);
  }

  @Override
  public Enumeration<String> getHeaders(String s) {
    List<String> strings = exchangeRequest.getHeaders().get(s);
    // 将 List<String> 转换为 Enumeration<String>
    return (strings != null)
        ? Collections.enumeration(strings)
        : Collections.emptyEnumeration();

  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return null;

  }

  @Override
  public int getIntHeader(String s) {
    return Integer.parseInt(getHeader(s));
  }

  @Override
  public String getMethod() {
    return exchangeRequest.getMethod().toString();
  }

  @Override
  public String getPathInfo() {
    return null;
  }

  @Override
  public String getPathTranslated() {
    return null;
  }

  @Override
  public String getContextPath() {
    return null;
  }

  @Override
  public String getQueryString() {
    return exchangeRequest.getQueryString();
  }

  @Override
  public String getRemoteUser() {
    return null;
  }

  @Override
  public boolean isUserInRole(String s) {
    return false;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }

  @Override
  public String getRequestedSessionId() {
    return null;
  }

  @Override
  public String getRequestURI() {
    return exchangeRequest.getUrl();
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer sb = new StringBuffer(128);
    sb.append(getScheme()).append("://").append(getServerName()).append(':').append(getServerPort()).append(getRequestURI());
    return sb;
  }

  @Override
  public String getServletPath() {
    return null;
  }

  @Override
  public HttpSession getSession(boolean b) {
    return null;
  }

  @Override
  public HttpSession getSession() {
    return null;
  }

  @Override
  public String changeSessionId() {
    return null;
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return false;
  }

  @Override
  public boolean authenticate(HttpServletResponse httpServletResponse)
      throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(String s, String s1) throws ServletException {

  }

  @Override
  public void logout() throws ServletException {

  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    return null;
  }

  @Override
  public Part getPart(String s) throws IOException, ServletException {
    return null;
  }

  @Override
  public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass)
      throws IOException, ServletException {
    return null;
  }

  @Override
  public Object getAttribute(String s) {
    return exchangeRequest.getAttribute(s);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return null;
  }

  @Override
  public String getCharacterEncoding() {
    return PropertyUtil.getProperty("server.encoding", "UTF-8");
  }

  @Override
  public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

  }

  @Override
  public int getContentLength() {
    return getIntHeader("Content-Length");
  }

  @Override
  public long getContentLengthLong() {
    return getContentLength();
  }

  @Override
  public String getContentType() {
    return getHeader("Content-Type");
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (!this.inputCalled) {
      this.inputCalled = true;
      return new ServletInputStreamImpl(this.exchangeRequest.getRequestBody());
    }
    throw new IllegalStateException("Cannot reopen input stream after " + (this.inputCalled ? "getInputStream()" : "getReader()") + " was called.");
  }

  @Override
  public String getParameter(String s) {
    return  this.exchangeRequest.getParameter(s);
  }


  @Override
  public Enumeration<String> getParameterNames() {
    return null;
  }

  @Override
  public String[] getParameterValues(String s) {
    return this.exchangeRequest.getParameterValues(s);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return null;
  }

  @Override
  public String getProtocol() {
     return "HTTP/1.1";
  }

  @Override
  public String getScheme() {
    return "http";
  }

  @Override
  public String getServerName() {
    return null;
  }

  @Override
  public int getServerPort() {
    return 0;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return null;
  }

  @Override
  public String getRemoteAddr() {
    return null;
  }

  @Override
  public String getRemoteHost() {
    return null;
  }

  @Override
  public void setAttribute(String s, Object o) {

    this.exchangeRequest.setAttribute(s,o);

  }

  @Override
  public void removeAttribute(String s) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return null;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public RequestDispatcher getRequestDispatcher(String s) {
    return null;
  }

  @Override
  public String getRealPath(String s) {
    return null;
  }

  @Override
  public int getRemotePort() {
    return 0;
  }

  @Override
  public String getLocalName() {
    return null;
  }

  @Override
  public String getLocalAddr() {
    return null;
  }

  @Override
  public int getLocalPort() {
    return 0;
  }

  @Override
  public ServletContext getServletContext() {
    return null;
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    return null;
  }

  @Override
  public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
      throws IllegalStateException {
    return null;
  }

  @Override
  public boolean isAsyncStarted() {
    return false;
  }

  @Override
  public boolean isAsyncSupported() {
    return false;
  }

  @Override
  public AsyncContext getAsyncContext() {
    return null;
  }

  @Override
  public DispatcherType getDispatcherType() {
    return null;
  }
}

