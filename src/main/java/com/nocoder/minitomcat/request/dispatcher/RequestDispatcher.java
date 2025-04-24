package com.nocoder.minitomcat.request.dispatcher;


import com.nocoder.minitomcat.exception.base.ServletException;
import com.nocoder.minitomcat.request.Request;
import com.nocoder.minitomcat.response.Response;

import java.io.IOException;

public interface RequestDispatcher {
    
    void forward(Request request, Response response) throws ServletException, IOException;
}
