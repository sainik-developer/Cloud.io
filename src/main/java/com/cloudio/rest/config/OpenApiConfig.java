package com.cloudio.rest.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Cloud.io API",
                version = "v1",
                description = "This app provides REST APIs for Cloud.io",
                contact = @Contact(
                        name = "sainik",
                        email = "schattapadhyay@ask-fast.com"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:9095/",
                        description = "sandbox Server"
                )
        }
)
public class OpenApiConfig {
}
