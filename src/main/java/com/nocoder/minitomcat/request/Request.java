package com.nocoder.minitomcat.request;


import com.nocoder.minitomcat.constant.CharConstant;
import com.nocoder.minitomcat.constant.CharsetProperties;
import com.nocoder.minitomcat.context.ServletContext;
import com.nocoder.minitomcat.context.WebApplication;
import com.nocoder.minitomcat.cookie.Cookie;
import com.nocoder.minitomcat.enumeration.RequestMethod;
import com.nocoder.minitomcat.exception.RequestInvalidException;
import com.nocoder.minitomcat.exception.RequestParseException;
import com.nocoder.minitomcat.network.handler.AbstractRequestHandler;
import com.nocoder.minitomcat.request.dispatcher.RequestDispatcher;
import com.nocoder.minitomcat.request.dispatcher.impl.ApplicationRequestDispatcher;
import com.nocoder.minitomcat.session.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>作者： zcq</p>
 * <p>文件名称: Request </p>
 * <p>描述: [类型描述] </p>
 * <p>创建时间: 2025/4/23 </p>
 *
 * @author <a href="mail to: 2928235428@qq.com" rel="nofollow">作者</a>
 * @version 1.0
 **/

@Slf4j
public class Request {
  private final Logger logger = LoggerFactory.getLogger(Request.class);



  private AbstractRequestHandler requestHandler;

  private   byte[] requestBody;
  private RequestMethod method;
  private String url;
  private Map<String, List<String>> params;

  private String  queryString;
  private Map<String, List<String>> headers;
  private Map<String, Object> attributes;
  private ServletContext servletContext;
  private Cookie[] cookies;
  private HttpSession session;

  public AbstractRequestHandler getRequestHandler() {
    return requestHandler;
  }

  public void setRequestHandler(AbstractRequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  public RequestMethod getMethod() {
    return method;
  }

  public void setMethod(RequestMethod method) {
    this.method = method;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Map<String, List<String>> getParams() {
    return params;
  }

  public void setParams(Map<String, List<String>> params) {
    this.params = params;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = headers;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public Cookie[] getCookies() {
    return cookies;
  }

  public void setCookies(Cookie[] cookies) {
    this.cookies = cookies;
  }

  public void setSession(HttpSession session) {
    this.session = session;
  }

  public String getQueryString() {
    return queryString;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public byte[] getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(byte[] requestBody) {
    this.requestBody = requestBody;
  }

  /**
   * 获取queryString或者body（表单格式）的键值类型的数据
   * @param key
   * @return
   */
  public String getParameter(String key) {
    List<String> params = this.params.get(key);
    if(params == null) {
      return null;
    }
    return params.get(0);
  }

  public String[] getParameterValues(String s) {
     List<String> params = this.params.get(s);
     return (params != null)
        ? params.toArray(new String[0])
        : null;
  }


  /**
   * 解析HTTP请求
   * 读取请求体只能使用字节流，使用字符流读不到
   * @param data
   * @throws RequestParseException
   */
  public Request(byte[] data) throws RequestParseException, RequestInvalidException, IOException {
    this.attributes = new HashMap<>();
    String[] lines = null;
    try {
      //支持中文，对中文进行URL解码
      lines = URLDecoder.decode(new String(data, CharsetProperties.UTF_8_CHARSET), CharsetProperties.UTF_8).split(CharConstant.CRLF);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    logger.info("Request读取完毕");
    logger.info("请求行: {}", Arrays.toString(lines));
    if (lines != null && lines.length <= 1) {
      throw new RequestInvalidException();
    }
    try {
      if (lines != null) {
        parseHeaders(lines);
      }
      // 解析请求体,将此时的请求体转为byte字节数组,也就是String ==> byte[]
      if (lines != null) {
        this.requestBody = lines[lines.length - 1].getBytes();
      }

      if (headers.containsKey("Content-Length") && !headers.get("Content-Length").get(0).equals("0")) {
        if (lines != null) {
          parseBody(lines[lines.length - 1]);
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RequestParseException();
    }

    WebApplication.getServletContext().afterRequestCreated(this);
  }

  public void setAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    return attributes.get(key);
  }

  public RequestDispatcher getRequestDispatcher(String url) {
    return new ApplicationRequestDispatcher(url);
  }

  /**
   * 如果请求报文中携带JSESSIONID这个Cookie，那么取出对应的session
   * 否则创建一个Session，并在响应报文中添加一个响应头Set-Cookie: JSESSIONID=D5A5C79F3C8E8653BC8B4F0860BFDBCD
   * 所有从请求报文中得到的Cookie，都会在响应报文中返回
   * 服务器只会在客户端第一次请求响应的时候，在响应头上添加Set-Cookie：“JSESSIONID=XXXXXXX”信息，
   * 接下来在同一个会话的第二第三次响应头里，是不会添加Set-Cookie：“JSESSIONID=XXXXXXX”信息的；
   * 即，如果在Cookie中读到的JSESSIONID，那么不会创建新的Session，也不会在响应头中加入Set-Cookie：“JSESSIONID=XXXXXXX”
   * 如果没有读到，那么会创建新的Session，并在响应头中加入Set-Cookie：“JSESSIONID=XXXXXXX”
   * 如果没有调用getSession，那么不会创建新的Session
   *
   * @param createIfNotExists 如果为true，那么在不存在session时会创建一个新的session；否则会直接返回null
   * @return HttpSession
   */
  public HttpSession getSession(boolean createIfNotExists) {
    if (session != null) {
      return session;
    }
    for (Cookie cookie : cookies) {
      if (cookie.getKey().equals("JSESSIONID")) {
        HttpSession currentSession = servletContext.getSession(cookie.getValue());
        if (currentSession != null) {
          session = currentSession;
          return session;
        }
      }
    }
    if (!createIfNotExists) {
      return null;
    }
    session = servletContext.createSession(requestHandler.getResponse());
    return session;
  }

  public HttpSession getSession() {
    return getSession(true);
  }

  public String getServletPath() {
    return url;
  }





  private void parseHeaders(String[] lines) {
    logger.info("解析请求头");
    String firstLine = lines[0];
    //解析方法
    String[] firstLineSlices = firstLine.split(CharConstant.BLANK);
    this.method = RequestMethod.valueOf(firstLineSlices[0]);
    logger.debug("method:{}", this.method);

    //解析URL
    String rawURL = firstLineSlices[1];
    String[] urlSlices = rawURL.split("\\?");
    this.url = urlSlices[0];
    this.queryString = urlSlices.length > 1 ? urlSlices[1] : null;
    logger.debug("url:{}", this.url);

    //解析URL参数
    if (urlSlices.length > 1) {
      parseParams(urlSlices[1]);
    }
    logger.debug("params:{}", this.params);

    //解析请求头
    String header;
    this.headers = new HashMap<>();
    for (int i = 1; i < lines.length; i++) {
      header = lines[i];
      if (header.isEmpty()) {
        break;
      }
      int colonIndex = header.indexOf(':');
      String key = header.substring(0, colonIndex);
      String[] values = header.substring(colonIndex + 2).split(",");
      headers.put(key, Arrays.asList(values));
    }
    logger.debug("headers:{}", this.headers);

    //解析Cookie
    if (headers.containsKey("Cookie")) {
      String[] rawCookies = headers.get("Cookie").get(0).split("; ");
      this.cookies = new Cookie[rawCookies.length];
      for (int i = 0; i < rawCookies.length; i++) {
        String[] kv = rawCookies[i].split("=");
        this.cookies[i] = new Cookie(kv[0], kv[1]);
      }
      headers.remove("Cookie");
    } else {
      this.cookies = new Cookie[0];
    }
    logger.info("Cookies:{}", Arrays.toString(cookies));
  }

  private void parseBody(String body) {
    logger.info("解析请求体");
    byte[] bytes = body.getBytes(CharsetProperties.UTF_8_CHARSET);
    List<String> lengths = this.headers.get("Content-Length");
    if (lengths != null) {
      int length = Integer.parseInt(lengths.get(0));
      logger.info("length:{}", length);
      parseParams(new String(bytes, 0, Math.min(length,bytes.length), CharsetProperties.UTF_8_CHARSET).trim());
    } else {
      parseParams(body.trim());
    }
    if (this.params == null) {
      this.params = new HashMap<>();
    }
  }

  private void parseParams(String params) {
    String[] urlParams = params.split("&");
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    for (String param : urlParams) {
      String[] kv = param.split("=");
      String key = kv[0];
      String[] values = kv[1].split(",");

      this.params.put(key, Arrays.asList(values));
    }
  }
}
