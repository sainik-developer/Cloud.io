package com.cloudio.rest.config;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BraintreeConfig {

    @Value("${payment.bt.environment}")
    private String ENVIRONMENT;

    @Value("${payment.bt.merchantId}")
    private String MERCHANT_ID;

    @Value("${payment.bt.publicKey}")
    private String PUBLIC_KEY;

    @Value("${payment.bt.privateKey}")
    private String PRIVATE_KEY;

    @Bean
    public BraintreeGateway createGateway() {
        return new BraintreeGateway(
                Environment.SANDBOX,
                MERCHANT_ID,
                PUBLIC_KEY,
                PRIVATE_KEY
        );
    }
}
