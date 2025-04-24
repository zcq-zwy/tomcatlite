package com.nocoder.minitomcat.request.dispatcher.impl;

import com.nocoder.minitomcat.constant.CharsetProperties;
import com.nocoder.minitomcat.exception.ResourceNotFoundException;
import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.request.dispatcher.RequestDispatcher;
import com.nocoder.minitomcat.resource.ResourceHandler;
import com.nocoder.minitomcat.response.Response;
import com.nocoder.minitomcat.template.TemplateResolver;
import com.nocoder.minitomcat.util.IOUtil;
import com.nocoder.minitomcat.util.MimeTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 请求转发器
 */
@Slf4j
public class ApplicationRequestDispatcher implements RequestDispatcher {
    private final Logger logger = LoggerFactory.getLogger(ApplicationRequestDispatcher.class);

    private String url;

    public ApplicationRequestDispatcher() {
    }

    public ApplicationRequestDispatcher(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void forward(Request request, Response response) throws ServletException, IOException {
        if (ResourceHandler.class.getResource(url) == null) {
            throw new ResourceNotFoundException();
        }
        logger.info("forward至 {} 页面",url);
        String body = TemplateResolver.resolve(new String(IOUtil.getBytesFromFile(url), CharsetProperties.UTF_8_CHARSET),request);
        response.setContentType(MimeTypeUtil.getTypes(url));
        response.setBody(body.getBytes(CharsetProperties.UTF_8_CHARSET));
    }
}
