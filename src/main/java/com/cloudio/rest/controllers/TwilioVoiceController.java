package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.TwilioCallRequestDTO;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/twilio/voice")
public class TwilioVoiceController {
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;

    @PostMapping(value = "/init", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> handleInit(final TwilioCallRequestDTO twilioCallRequestDTO) {
        log.info("init body from call is {}", twilioCallRequestDTO);
        return Mono.just(twilioCallRequestDTO)
                .filter(twilioCallRequestDto -> twilioCallRequestDto.getFrom().startsWith("client"))
                .map(twilioCallRequestDto -> new VoiceResponse.Builder().say(new Say.Builder("One to one call for transfer is not yet implemented in cloud.io").build()).reject(new Reject.Builder().reason(Reject.Reason.REJECTED).build()).build())
                .map(VoiceResponse::toXml)
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().gather(new Gather.Builder().finishOnKey("*").action("/twilio/voice/dtmf").build()).build().toXml()));
    }

    @PostMapping(value = "/dtmf", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> handleDTMFEntry(final TwilioCallRequestDTO twilioCallRequestDTO) {
        final String adapterNumber = twilioCallRequestDTO.getTo().trim() + "," + twilioCallRequestDTO.getDigits() + "*";
        log.info("/dtmf is called {} where adapter number is {}", twilioCallRequestDTO, adapterNumber);
        return companyRepository.findByAdapterNumber(adapterNumber)
                .doOnNext(companyDo -> log.info("adapter number is found and related company {}", companyDo))
                .flatMap(companyDo -> accountRepository.findByCompanyIdAndStatus(companyDo.getCompanyId(), AccountStatus.ACTIVE)
                        .doOnNext(accountDo -> log.info(""))
                        .map(accountDo -> new Client.Builder().identity(accountDo.getAccountId()).build())
                        .doOnNext(client -> log.info(""))
                        .collectList()
                        .doOnNext(clients -> log.info("total number if client are {}", clients.size()))
                        .map(clients -> {
                            final Dial.Builder builder = new Dial.Builder();
                            clients.forEach(builder::client);
                            return builder.build();
                        })
                        .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                        .map(VoiceResponse::toXml))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().say(new Say.Builder("Adapter Number is not found").build()).build().toXml()));
    }
}
