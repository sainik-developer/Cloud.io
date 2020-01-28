package com.cloudio.rest.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Cloud.io API",
                version = "v1",
                description = "This app provides REST APIs for Cloud.io",
                contact = @Contact(
                        name = "sainik",
                        email = "schattapadhyay@ask-fast.com"
                )
        )
)
public class OpenApiConfig {
}
