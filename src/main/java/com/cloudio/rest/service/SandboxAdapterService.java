package com.cloudio.rest.service;

import com.cloudio.rest.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile(value = {"sandbox", "local"})
public class SandboxAdapterService implements AdapterService {
    private final CompanyRepository companyRepository;
    private final RandomNumberGenerator randomNumberGenerator;

    @Value("${twilio.adapter.number}")
    private String baseAdapterNumberForSandbox;

    @Override
    public Mono<String> getAvailableAdapter() {
        return uniqueAdapterNumber();
    }

    private Mono<String> uniqueAdapterNumber() {
        final String newAdapterNumber = baseAdapterNumberForSandbox + "," + randomNumberGenerator.generateRandomNumberOfLength(4) + "*";
        return companyRepository.findByAdapterNumber(baseAdapterNumberForSandbox + "," + randomNumberGenerator.generateRandomNumberOfLength(4) + "*")
                .flatMap(companyDo -> uniqueAdapterNumber())
                .switchIfEmpty(Mono.just(newAdapterNumber));
    }
}
