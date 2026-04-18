package com.example.store.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderOpenApiConfiguration {

    @Bean
    public OpenAPI orderStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Online Store API")
                        .description("Модуль заказов и связанные операции")
                        .version("1.0.0"));
    }
}
