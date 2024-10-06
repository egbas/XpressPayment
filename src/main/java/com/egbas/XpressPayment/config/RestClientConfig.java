package com.egbas.XpressPayment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${payment.api.url}")
    private String baseurl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(baseurl)
                .build();
    }

}
