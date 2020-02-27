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
import org.apache.commons.lang3.StringUtils;
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
                .filter(twilioCallRequestDto -> !StringUtils.isEmpty(twilioCallRequestDto.getFrom()))
                .filter(twilioCallRequestDto -> twilioCallRequestDto.getFrom().startsWith("client") && twilioCallRequestDto.getTo().startsWith("client"))
                .flatMap(twilioCallRequestDto -> accountRepository.findByAccountIdAndStatus("CIO:ACC:" + twilioCallRequestDto.getFrom().substring(15).replace("_", "-"), AccountStatus.ACTIVE)
                        .map(accounto -> {
                            twilioCallRequestDto.setFromPhoneNumber(accounto.getPhoneNumber());
                            twilioCallRequestDto.setFromNameCloudIO(accounto.getFirstName() + StringUtils.defaultString(accounto.getLastName()));
                            return twilioCallRequestDto;
                        }))
                .map(twilioCallRequestDto -> new VoiceResponse.Builder()
                        .dial(new Dial.Builder()
                                .client(new Client.Builder(twilioCallRequestDto.getTo().substring(7))
                                        .parameter(new Parameter.Builder().name("fromPhoneNumber").value(twilioCallRequestDto.getFromPhoneNumber()).build())
                                        .parameter(new Parameter.Builder().name("fromNameCloudIO").value(twilioCallRequestDto.getFromNameCloudIO()).build())
                                        .parameter(new Parameter.Builder().name("InitiatorPhoneNumber").value(twilioCallRequestDto.getInitiatorPhoneNumber()).build())
                                        .build())
                                .build())
                        .build())
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
                .map(s -> ResponseDTO.builder().data(s).build())
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }

    @PostMapping(value = "/transfer", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseDTO> handleTransfer(@RequestHeader("accountId") final String fromAccountId,
                                            @NotEmpty @RequestParam("callSid") final String callSid,
                                            @NotEmpty @RequestParam("toAccount") final String toAccount) {
        return accountRepository.findByAccountIdAndStatus(fromAccountId, AccountStatus.ACTIVE)
                .doOnNext(accountDo -> log.info("from account Id found and ACTIVE and details are {}", accountDo))
                .flatMap(fromAccountDo -> accountRepository.findByAccountIdAndStatus(toAccount, AccountStatus.ACTIVE)
                        .doOnNext(accountDo -> log.info("to account Id found and details are {}", accountDo))
                        .filter(toAccountDo -> fromAccountDo.getCompanyId().equals(toAccountDo.getCompanyId()))
                        .doOnNext(accountDo -> log.info("both are part of same company, where company Id is {}", accountDo.getCompanyId()))
                        .flatMap(toAccountDo -> twilioService.transferCall(callSid, toAccount))
                        .doOnNext(newCallSid -> log.info("call is transferred successfully where new call sid is {}", newCallSid))
                        .map(newCallSid -> ResponseDTO.builder().data(newCallSid).build()))
                .switchIfEmpty(Mono.error(AccountNotExistException::new));
    }
}
