package com.hotel.assignment.config;

import com.hotel.assignment.payment.client.ApiClient;
import com.hotel.assignment.payment.client.api.DefaultApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PaymentServiceProperties.class)
public class ClientConfig {

    @Bean
    public DefaultApi paymentStatusApi(
            PaymentServiceProperties properties
    ) {
        HttpClientSettings settings = HttpClientSettings.defaults()
                .withTimeouts(
                        properties.connectTimeout(),
                        properties.readTimeout()
                );

        ClientHttpRequestFactory requestFactory =
                ClientHttpRequestFactoryBuilder.detect().build(settings);

        RestClient restClient = ApiClient.buildRestClientBuilder()
                .requestFactory(requestFactory)
                .build();

        ApiClient apiClient = new ApiClient(restClient);
        apiClient.setBasePath(properties.baseUrl().toString());

        return new DefaultApi(apiClient);
    }
}