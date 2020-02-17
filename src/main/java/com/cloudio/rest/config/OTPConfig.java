package com.cloudio.rest.config;

import com.cloudio.rest.pojo.OTPGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OTPConfig {
    @Bean
    @Profile({"sandbox","local"})
    public OTPGenerator staticOTPGenerator() {
        return () -> "111111";
    }

    @Bean
    @Profile("prod")
    public OTPGenerator randomOTPGenerator() {
        return () -> String.valueOf((int) Math.floor(100000 + Math.random() * 900000));
    }
}
