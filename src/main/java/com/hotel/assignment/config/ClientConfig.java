package com.hotel.assignment.config;

import com.yourcompany.app.api.ApiClient;
import com.yourcompany.app.api.client.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Value("${bank.api.base-url}")
    private String bankApiBaseUrl;

    @Bean
    public DefaultApi paymentStatusApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(bankApiBaseUrl);
        return new DefaultApi(apiClient);
    }
}
