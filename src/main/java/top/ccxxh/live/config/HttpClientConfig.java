package top.ccxxh.live.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.ccxh.httpclient.autoconfigure.HttpClientFactory;
import top.ccxh.httpclient.autoconfigure.HttpClientProperties;
import top.ccxh.httpclient.service.HttpClientService;

/**
 * 多client配置实例
 * @author qing
 */
@Configuration
public class HttpClientConfig {

    @Bean("testAgentHttpClientProperties")
    @ConfigurationProperties(prefix = "test-agent")
    public HttpClientProperties geHttpClientProperties() {
        return new HttpClientProperties();
    }
    @Bean("testAgent")
    public HttpClientService getHttpClientService(@Qualifier("testAgentHttpClientProperties") HttpClientProperties httpClientProperties ) {
        return HttpClientFactory.getHttpClientService(httpClientProperties);
    }
}
