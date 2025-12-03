package ru.vserg.loggingstarter.feign;

import feign.Logger;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import ru.vserg.loggingstarter.service.LoggingService;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class FeignRequestLogger extends Logger {
    private boolean logFeignRequestsBody;

    @Autowired
    private LoggingService loggingService;


    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        loggingService.logFeignRequest(request);

        if (logFeignRequestsBody && request.body() != null && request.body().length > 0) {
            String url = request.url();
            URI uri = URI.create(url);
            RequestTemplate template = new RequestTemplate()
                    .method(request.httpMethod())
                    .uri(uri.getRawPath())
                    .headers(request.headers())
                    .body(request.body(), StandardCharsets.UTF_8);
            loggingService.logFeignBody(template);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response,
                                              long elapsedTime) throws IOException {
        String responseBody = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);

        loggingService.logFeignResponse(response, responseBody);

        return response.toBuilder()
                .body(responseBody, StandardCharsets.UTF_8)
                .build();
    }

    @Override
    protected void log(String s, String s1, Object... objects) {
        //имплементация не требуется
    }

    @Value("${logging.web-logging.log-feign-requests-body:false}")
    public void setLogFeignRequestsBody(boolean logFeignRequestsBody) {
        this.logFeignRequestsBody = logFeignRequestsBody;
    }

    @Autowired
    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }
}
