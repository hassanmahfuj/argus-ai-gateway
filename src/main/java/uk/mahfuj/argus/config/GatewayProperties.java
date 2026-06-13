package uk.mahfuj.argus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway.proxy")
public class GatewayProperties {

    private String apiKey = "";
    private int timeout = 120;
    private Upstream upstream = new Upstream();

    @Getter
    @Setter
    public static class Upstream {
        private String openai = "https://api.z.ai/api/coding/paas/v4";
        private String anthropic = "https://api.z.ai/api/anthropic";
    }
}
