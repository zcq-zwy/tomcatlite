package com.nocoder.minitomcat.resource;

import com.nocoder.minitomcat.constant.CharsetProperties;
import com.nocoder.minitomcat.exception.RequestParseException;
import com.nocoder.minitomcat.exception.ResourceNotFoundException;
import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.exception.handler.ExceptionHandler;
import com.nocoder.minitomcat.network.wrapper.nio.NioSocketWrapper;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;
import com.nocoder.minitomcat.template.TemplateResolver;
import com.nocoder.minitomcat.util.IOUtil;
import com.nocoder.minitomcat.util.MimeTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 用于处理静态资源
 * @author 29282
 */
@Slf4j
public class ResourceHandler {
    private final Logger logger = LoggerFactory.getLogger(ResourceHandler.class);

    private ExceptionHandler exceptionHandler;

    public ResourceHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public void handle(Request request, Response response, NioSocketWrapper socketWrapper) {
        String url = request.getUrl();
        try {
            if (ResourceHandler.class.getResource(url) == null) {
                logger.info("找不到该资源:{}", url);
                throw new ResourceNotFoundException();
            }
            byte[] body = IOUtil.getBytesFromFile(url);
            if (url.endsWith(".html")) {
                body = TemplateResolver
                        .resolve(new String(body, CharsetProperties.UTF_8_CHARSET), request)
                        .getBytes(CharsetProperties.UTF_8_CHARSET);
            }
            response.setContentType(MimeTypeUtil.getTypes(url));
            response.setBody(body);
        } catch (IOException e) {
            exceptionHandler.handle(new RequestParseException(), response, socketWrapper);
        } catch (ServletException e) {
            exceptionHandler.handle(e, response, socketWrapper);
        }
    }
}