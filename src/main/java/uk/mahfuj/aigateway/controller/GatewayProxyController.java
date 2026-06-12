package uk.mahfuj.aigateway.controller;

import uk.mahfuj.aigateway.config.GatewayProperties;
import uk.mahfuj.aigateway.service.GatewayProxyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/v1")
public class GatewayProxyController {

    private final GatewayProxyService proxyService;
    private final GatewayProperties properties;

    public GatewayProxyController(final GatewayProxyService proxyService,
                                  final GatewayProperties properties) {
        this.proxyService = proxyService;
        this.properties = properties;
    }

    @RequestMapping("/anthropic/**")
    public void proxyAnthropic(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String subPath = proxyService.extractSubPath(request, "/v1/anthropic");
        proxyService.forward(request, response, properties.getUpstream().getAnthropic(), subPath);
    }

    @RequestMapping("/**")
    public void proxyOpenAI(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String subPath = proxyService.extractSubPath(request, "/v1");
        proxyService.forward(request, response, properties.getUpstream().getOpenai(), subPath);
    }
}
