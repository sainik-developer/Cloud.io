package com.cloudio.rest.service;

import com.cloudio.rest.dto.CompanyDTO;
import com.cloudio.rest.dto.CompanySettingDTO;
import com.cloudio.rest.entity.AccountDO;
import com.cloudio.rest.exception.RingInOrderIsEmptyException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.pojo.CompanyStatus;
import com.cloudio.rest.pojo.RingType;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompanyService {
    private final AdapterService adapterService;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;


    public Mono<Boolean> isCompanyNameUnique(final String companyName) {
        return companyRepository.findByName(companyName)
                .map(companyDO -> Boolean.FALSE)
                .switchIfEmpty(Mono.just(Boolean.TRUE));
    }

    public Mono<CompanyDTO> createCompany(final CompanyDTO companyDTO) {
        return adapterService.getAvailableAdapter()
                .map(s -> {
                    companyDTO.setCompanyId("CIO:COM:" + UUID.randomUUID().toString());
                    companyDTO.setCompanyStatus(CompanyStatus.NOT_VERIFIED);
                    companyDTO.setAdapterNumber(s);
                    companyDTO.setCompanySetting(CompanySettingDTO.builder().isVoiceMessage(false).ringType(RingType.ALL_AT_ONCE).build());
                    return companyDTO;
                });
    }

    public Mono<CompanySettingDTO> checkIfAccountPartOfCompany(final String companyId, final CompanySettingDTO companySettingDTO) {
        if (companySettingDTO.getRingType() == RingType.ALL_AT_ONCE) {
            return Mono.just(companySettingDTO);
        }
        return Flux.fromIterable(companySettingDTO.getRingOrderAccountIds())
                .flatMap(accountId -> accountRepository.findByAccountIdAndStatus(accountId, AccountStatus.ACTIVE))
                .filter(accountDo -> accountDo.getCompanyId().equals(companyId))
                .map(AccountDO::getAccountId)
                .collectList()
                .filter(accountIds -> !accountIds.isEmpty())
                .map(accountDos -> {
                    companySettingDTO.setRingOrderAccountIds(accountDos);
                    return companySettingDTO;
                })
                .switchIfEmpty(Mono.error(RingInOrderIsEmptyException::new));
    }
}
