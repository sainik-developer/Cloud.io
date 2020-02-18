package com.cloudio.rest.controllers;

import com.cloudio.rest.dto.ResponseDTO;
import com.cloudio.rest.dto.TwilioCallRequestDTO;
import com.cloudio.rest.exception.AccountNotExistException;
import com.cloudio.rest.pojo.AccountStatus;
import com.cloudio.rest.repository.AccountRepository;
import com.cloudio.rest.repository.CompanyRepository;
import com.cloudio.rest.service.TwilioService;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;

@Log4j2
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/twilio/voice")
public class TwilioVoiceController {
    private final CompanyRepository companyRepository;
    private final AccountRepository accountRepository;
    private final TwilioService twilioService;

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
                        .map(accountDo -> new Client.Builder().identity(twilioService.createTwilioCompatibleClientId(accountDo.getAccountId())).build())
                        .doOnNext(client -> log.info(""))
                        .collectList()
                        .doOnNext(clients -> log.info("total number of clients are {}", clients.size()))
                        .map(clients -> {
                            final Dial.Builder builder = new Dial.Builder();
                            clients.forEach(builder::client);
                            return builder.build();
                        })
                        .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                        .map(VoiceResponse::toXml)
                        .doOnNext(xml -> log.info("dial Twilio xml is {}", xml)))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().say(new Say.Builder("Adapter Number is not found").build()).build().toXml()));
    }

    @PostMapping(value = "/hold", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseDTO> handleHold(@RequestHeader("accountId") final String fromAccountId,
                                        @NotEmpty @RequestParam("callSid") final String callSid) {
        return accountRepository.findByAccountIdAndStatus(fromAccountId, AccountStatus.ACTIVE)
                .flatMap(accountDo -> twilioService.holdIncomingCallToAdapter(fromAccountId, callSid))
                .map(s -> ResponseDTO.builder().message(s).build())
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @PostMapping(value = "/transfer", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseDTO> handleTransfer(@RequestHeader("accountId") final String fromAccountId,
                                            @NotEmpty @RequestParam("callSid") final String callSid,
                                            @NotEmpty @RequestParam("toAccount") final String toAccount) {
        return accountRepository.findByAccountIdAndStatus(fromAccountId, AccountStatus.ACTIVE)
                .flatMap(fromAccountDo -> accountRepository.findByAccountIdAndStatus(toAccount, AccountStatus.ACTIVE)
                        .filter(toAccountDo -> fromAccountDo.getCompanyId().equals(toAccountDo.getCompanyId()))
                        .flatMap(toAccountDo -> twilioService.transferCall(callSid, toAccount))
                        .map(s -> ResponseDTO.builder().message(s).build()))
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }
}
