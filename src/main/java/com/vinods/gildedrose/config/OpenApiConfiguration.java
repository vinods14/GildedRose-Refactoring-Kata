package com.vinods.gildedrose.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Gilded Rose Inventory API",
                version = "1.0.0",
                description = "REST API for managing the Gilded Rose inventory and applying daily quality updates",
                contact = @Contact(name = "Vinod Sharma"),
                license = @License(name = "MIT")
        )
)
public class OpenApiConfiguration {
}
