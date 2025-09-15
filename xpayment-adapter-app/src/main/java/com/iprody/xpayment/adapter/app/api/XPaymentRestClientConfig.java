package com.iprody.xpayment.adapter.app.api;

import com.iprody.xpayment.adapter.app.api.client.DefaultApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class XPaymentRestClientConfig {

    /*
    Бин типа данных RestTemplate, который включает в себя
    следующие HTTP-заголовки на основе ранее описанной конфигурации
    для веб-клиента x-payment-api:
    - Authorization: Basic <username:password in Base64>
    - X-Pay-Account: <account>
     */
    @Bean
    RestTemplate xPaymentRestTemplate(
        @Value("${app.x-payment-api.client.username}") String username,
        @Value("${app.x-payment-api.client.password}") String password,
        @Value("${app.x-payment-api.client.account}") String xPayAccount
    ) {
        final RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((req, body, ex) -> {
            req.getHeaders().setBasicAuth(username, password);
            req.getHeaders().add("X-Pay-Account", xPayAccount);
            return ex.execute(req, body);
        });
        return rt;
    }

    /*
    Бин типа данных ApiClient, который создаётся на основе ранее
    созданного bean типа данных RestTemplate и устанавливает свойство
    base-path на основе ранее описанной конфигурации для веб-клиента
    app.x-payment-api.client.url
     */
    @Bean
    ApiClient xPaymentApiClient(
        @Value("${app.x-payment-api.client.url}") String xPaymentUrl,
        RestTemplate xPaymentRestTemplate
    ) {
        final ApiClient apiClient = new ApiClient(xPaymentRestTemplate);
        apiClient.setBasePath(xPaymentUrl);
        return apiClient;
    }

    @Bean
    DefaultApi defaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
}