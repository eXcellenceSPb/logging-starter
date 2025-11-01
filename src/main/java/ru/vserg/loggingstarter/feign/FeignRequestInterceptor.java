package ru.vserg.loggingstarter.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vserg.loggingstarter.service.LoggingService;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Autowired
    private LoggingService loggingService;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        loggingService.logFeignBody(requestTemplate);
    }
}
