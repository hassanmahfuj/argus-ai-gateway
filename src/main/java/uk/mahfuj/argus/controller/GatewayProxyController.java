package uk.mahfuj.argus.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.mahfuj.argus.service.GatewayProxyService;


/**
 * Thin proxy endpoints. Routing (Anthropic vs OpenAI upstream, {@code /v1} prefix
 * stripping) is decided by {@link uk.mahfuj.argus.service.proxy.UpstreamResolver}
 * in the service layer; the controller only maps URLs and delegates.
 */
@RestController
@RequestMapping("/v1")
public class GatewayProxyController {

    private final GatewayProxyService proxyService;

    public GatewayProxyController(final GatewayProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping("/anthropic/**")
    public void proxyAnthropic(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        proxyService.proxy(request, response);
    }

    @RequestMapping("/**")
    public void proxyOpenAI(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        proxyService.proxy(request, response);
    }
}
