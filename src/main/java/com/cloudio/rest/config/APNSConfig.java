package com.cloudio.rest.config;

import com.cloudio.rest.service.AWSS3Services;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class APNSConfig {
    private final AWSS3Services awss3Services;

    @Bean(destroyMethod = "close")
    @Profile(value = {"local", "sandbox"})
    ApnsClient createClientSandBox(@Value("${amazonProperties.sensitive.bucketName}") final String bucketName,
                                   @Value("${amazonProperties.apns.p12}") String apnsCertificateFileName, @Value("${apns.p12.password}") String apnP12Password) throws IOException {
        log.info("Going to create apns client with credential file named {} ", apnsCertificateFileName);
        return new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setClientCredentials(awss3Services.getFile(bucketName, apnsCertificateFileName).getObjectContent(), apnP12Password).build();
    }

    @Bean(destroyMethod = "close")
    @Profile("prod")
    ApnsClient createClientProd(@Value("${amazonProperties.sensitive.bucketName}") final String bucketName,
                                @Value("${amazonProperties.apns.p12}") String apnsCertificateFileName) throws IOException {
        log.info("Going to create apns client with credential file named {} ", apnsCertificateFileName);
        return new ApnsClientBuilder().setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .setClientCredentials(awss3Services.getFile(bucketName, apnsCertificateFileName).getObjectContent(), "").build();
    }
}
