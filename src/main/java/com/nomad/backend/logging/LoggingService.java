package com.nomad.backend.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log4j2
public class LoggingService {

    public void displayReq(HttpServletRequest request, Object body) {
        String correlationId = UUID.randomUUID().toString();
        ThreadContext.put("correlationId", correlationId);

        StringBuilder reqMessage = new StringBuilder();
        Map<String,String> parameters = getParameters(request);

        reqMessage.append("REQUEST  -> ");
        reqMessage.append("[").append(request.getMethod()).append("]");
        reqMessage.append(" [").append(request.getRequestURI()).append("] ");

        if(!parameters.isEmpty()) {
            reqMessage.append(" parameters = [").append(parameters).append("] ");
        }

        if(!Objects.isNull(body)) {
            reqMessage.append(" requestBody = [").append(body).append("]");
        }

        log.info("log Request: {}", reqMessage);
    }

    public void displayResp(HttpServletRequest request, HttpServletResponse response, Object body) {
        StringBuilder respMessage = new StringBuilder();
        Map<String,String> headers = getHeaders(response);
        respMessage.append("RESPONSE -> ");
        respMessage.append("[").append(request.getMethod()).append("]");
        respMessage.append(" [").append(response.getStatus()).append("]");

        if(!headers.isEmpty()) {
            respMessage.append(" ResponseHeaders = [").append(headers).append("]");
        }

        respMessage.append(" responseBody = [").append(body).append("]");

        char statusDigit = Integer.toString(response.getStatus()).charAt(0);
        switch (statusDigit) {
            case '2':
                log.info("logResponse: {}",respMessage);
                break;
            case '3':
                break;
            case '4':
                log.error("logResponse: {}", respMessage);
                break;
            case '5':
                log.error("logResponse: {}", respMessage);
                break;
            default:
                log.info("logResponse: {}",respMessage);
        }
        ThreadContext.remove("correlationId");
    }

    private Map<String,String> getHeaders(HttpServletResponse response) {
        Map<String,String> headers = new HashMap<>();
        Collection<String> headerMap = response.getHeaderNames();
        for(String str : headerMap) {
            headers.put(str,response.getHeader(str));
        }
        return headers;
    }

    private Map<String,String> getParameters(HttpServletRequest request) {
        Map<String,String> parameters = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while(params.hasMoreElements()) {
            String paramName = params.nextElement();
            String paramValue = request.getParameter(paramName);
            parameters.put(paramName,paramValue);
        }
        return parameters;
    }
}