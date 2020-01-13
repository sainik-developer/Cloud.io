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
                        url = "http://prod-alb-511964033.eu-west-1.elb.amazonaws.com/qringGroups",
                        description = "sandbox Server"
                )
        }
)
public class OpenApiConfig {
}
