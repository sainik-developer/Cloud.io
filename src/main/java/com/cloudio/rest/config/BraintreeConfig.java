package com.cloudio.rest.config;

import com.braintreegateway.BraintreeGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BraintreeConfig {

    @Value("${cloud.io.payment.environment}")
    private String ENVIRONMENT;

    @Value("${cloud.io.payment.merchantId}")
    private String MERCHANT_ID;

    @Value("${cloud.io.payment.publicKey}")
    private String PUBLIC_KEY;

    @Value("${cloud.io.payment.privateKey}")
    private String PRIVATE_KEY;

    @Bean
    public BraintreeGateway createGateway() {
        return new BraintreeGateway(
                ENVIRONMENT,
                MERCHANT_ID,
                PUBLIC_KEY,
                PRIVATE_KEY
        );
    }
}
