package com.cloudio.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

@Configuration
@EnableSwagger2WebFlux
public class Swagger2SpringBootConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .genericModelSubstitutes(Mono.class, Flux.class)
                .useDefaultResponseMessages(false)
//                .pathProvider(new DefaultPathProvider() {
//                    @Override
//                    public String getOperationPath(String operationPath) {
//                        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/qringGroups");
//                        return removeAdjacentForwardSlashes(uriComponentsBuilder.path(operationPath).build().toString());
//                    }
//                })
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
