package com.millenniumitesp.productinventoryservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productInventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Inventory Service API")
                        .description("REST API for managing product inventory - create, retrieve, " +
                                "partially update (price/stock), and delete (archived) products.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Vidura")
                                .email("your.email@millenniumitesp.com")));
    }
}