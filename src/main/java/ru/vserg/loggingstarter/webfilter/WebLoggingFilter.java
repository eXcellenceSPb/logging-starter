package ru.vserg.loggingstarter.webfilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.vserg.loggingstarter.util.HttpRequestUtils.formattedQueryString;

@Component
public class WebLoggingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(WebLoggingFilter.class);

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String method = request.getMethod();
        String requestURI = request.getRequestURI() + formattedQueryString(request);
        String headers = inlineHeaders(request);

        log.info("Запрос: {} {} {}", method, requestURI, headers);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            super.doFilter(request, responseWrapper, chain);

        } finally {
            String responseBody = "body=" + new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

            responseWrapper.copyBodyToResponse();

            String responseHeaders = headersFromResponseWrapper(responseWrapper);

            log.info("Ответ: {} {} {} {} {}", method, requestURI, response.getStatus(), responseHeaders, responseBody);
        }

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

    private String headersFromResponseWrapper(ContentCachingResponseWrapper responseWrapper) {
        String headers = responseWrapper.getHeaderNames().stream()
                .map(entry -> {
                    String headerValue = responseWrapper.getHeader(entry);

                    return entry + "=" + headerValue;
                })
                .collect(Collectors.joining(","));
        return addWrappingBracesToHeaders(headers);
    }

    private String addWrappingBracesToHeaders(String headers) {
        return "headers={" + headers + "}";
    }

}
