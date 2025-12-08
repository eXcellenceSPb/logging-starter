package ru.vserg.loggingstarter.service;

import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.vserg.loggingstarter.dto.RequestDirection;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.vserg.loggingstarter.util.HttpRequestUtils.formattedQueryString;

@Service
public class LoggingService {

    private static final Logger log = LoggerFactory.getLogger(LoggingService.class);

    public void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI() + formattedQueryString(request);
        String headers = inlineHeaders(request);

        log.info("Запрос: {} {} {} {}", RequestDirection.IN, method, requestURI, headers);
    }

    public void logRequestBody(HttpServletRequest request, Object body) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI() + formattedQueryString(request);

        log.info("Тело запроса: {} {} {} body={}", RequestDirection.IN, method, requestURI, body);
    }

    public void logResponse(HttpServletRequest request, HttpServletResponse response, String responseBody) {
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String headers = inlineHeaders(request);

        log.info("Ответ: {} {} {} {} {} body={}", RequestDirection.IN, method, requestURI, response.getStatus(), headers, responseBody);
    }

    public void logFeignRequest(Request request, boolean logBody) {
        String method = request.httpMethod().name();
        String requestURI = request.url();
        String headers = inlineHeaders(request.headers());
        if (logBody) {
            String body = new String(request.body(), StandardCharsets.UTF_8);
            log.info("Запрос: {} {} {} {} body={}", RequestDirection.OUT, method, requestURI, headers, body);
        } else {
            log.info("Запрос: {} {} {} {}", RequestDirection.OUT, method, requestURI, headers);
        }
    }

    public void logFeignResponse(Response response, String responseBody) {
        String method = response.request().httpMethod().name();
        String responseURI = response.request().url();
        int status = response.status();
        String headers = inlineHeaders(response.headers());

        log.info("Ответ: {} {} {} {} {} body={}", RequestDirection.OUT, method, responseURI, status, headers, responseBody);
    }

    private String inlineHeaders(HttpServletRequest request) {
        Map<String, String> headersMap = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(it -> it, request::getHeader));

        String headers = headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = entry.getValue();

                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(","));

        return addWrappingBracesToHeaders(headers);
    }

    private String inlineHeaders(Map<String, Collection<String>> headersMap) {
        String headers = headersMap.entrySet().stream()
                .map(entry -> {
                    String headerName = entry.getKey();
                    String headerValue = String.join(",", entry.getValue());

                    return headerName + "=" + headerValue;
                })
                .collect(Collectors.joining(","));

        return addWrappingBracesToHeaders(headers);
    }

    private String addWrappingBracesToHeaders(String headers) {
        return "headers={" + headers + "}";
    }

}
