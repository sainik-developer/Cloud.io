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
    private CompanyRepository companyRepository;
    private AccountRepository accountRepository;

    @PostMapping(value = "/init", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> handleInit(final TwilioCallRequestDTO twilioCallRequestDTO) {
        log.info("init body from call is {}", twilioCallRequestDTO);
        return Mono.just(twilioCallRequestDTO)
                .filter(twilioCallRequestDto -> twilioCallRequestDto.getFrom().startsWith("client"))
                .map(twilioCallRequestDto -> new VoiceResponse.Builder().say(new Say.Builder("One to one call for transfer is not yet implemented").build()).reject(new Reject.Builder().reason(Reject.Reason.REJECTED).build()).build())
                .map(VoiceResponse::toXml)
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().gather(new Gather.Builder().finishOnKey("*").action("/twilio/voice/dtmf").build()).build().toXml()));


//        Mono.just(new VoiceResponse.Builder().gather(new Gather.Builder().finishOnKey("*").action("/twilio/voice/dtmf").build()).build().toXml())
//                .map(s -> )
//        final VoiceResponse wrongResponse = new VoiceResponse.Builder().say(new Say.Builder("You are calling from wrong number").build()).reject(new Reject.Builder().reason(Reject.Reason.REJECTED).build()).build();
//        try {
//            if (twilioCallRequestDTO.getFrom().startsWith("client")) {
//                //TODO call to client for one to one VOIP call
//            } else {
//                return ResponseEntity.ok();
//            }
//        } catch (final Exception e) {
//            log.error("error in formatting the from phone number, either it's not from real phone number, may be browser call. prohibited to accept the incoming call other than phone number due to avoid misuse of api ");
//            return ResponseEntity.ok(wrongResponse.toXml());
//            // TODO play a message and terminate the call
//        }
    }

    @PostMapping(value = "/dtmf", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<String> handleDTMFEntry(final TwilioCallRequestDTO twilioCallRequestDTO) {
//        log.info("DTMF body from call is {}", twilioCallRequestDTO);
//        final VoiceResponse.Builder voiceResponseBuilder = new VoiceResponse.Builder();
//        VoiceResponse voiceResponse = null;
//        if (StringUtils.isEmpty(twilioCallRequestDTO.getDigits())) {
//            if (twilioCallRequestDTO.getFrom().startsWith("+31")) {
//                voiceResponse = voiceResponseBuilder.say(new Say.Builder("Invalid input").build()).build();
//            } else {
//                voiceResponse = voiceResponseBuilder.say(new Say.Builder("Invalid input").build()).build();
//            }
//            return ResponseEntity.ok(voiceResponse.toXml());
//        }
//        final AnalogCallRedisData analogCallRedisData = analogCallStatusRepository.findByCode(twilioCallRequestDTO.getDigits());


        return companyRepository.findByAdapterNumber(twilioCallRequestDTO.getTo() + "," + twilioCallRequestDTO.getDigits() + "*")
                .flatMap(companyDo -> accountRepository.findByCompanyIdAndStatus(companyDo.getCompanyId(), AccountStatus.ACTIVE)
                        .map(accountDo -> new Client.Builder().identity(accountDo.getAccountId()).build())
                        .collectList()
                        .map(clients -> {
                            final Dial.Builder builder = new Dial.Builder();
                            clients.forEach(builder::client);
                            return builder.build();
                        })
                        .map(dial -> new VoiceResponse.Builder().dial(dial).build())
                        .map(VoiceResponse::toXml))
                .switchIfEmpty(Mono.just(new VoiceResponse.Builder().say(new Say.Builder("Adapter Number is not found").build()).build().toXml()));

//        if (analogCallRedisData != null) {
//            log.info("retrieved from redis {}", analogCallRedisData);
//            final GroupDO groupDO = groupRepository.findByGroupId(analogCallRedisData.getGroupId());
//            if (groupDO != null) {
//                log.info("group found and details are {}", groupDO);
//                final List<AccountDO> members = accountRepository.findByAccountIdsAndStatus(groupDO.getMembers(), AccountStatus.ACTIVE);
//                final Dial.Builder dialBuilder = new Dial.Builder();
//                if (members != null && members.size() > 0) {
//                    members.stream().map(AccountDO::getPhoneNumber).map(phoneNumber -> new Number.Builder(phoneNumber).build()).forEach(dialBuilder::number);
//                    voiceResponseBuilder.say(new Say.Builder("Setting up a call with the owners of this house. For more information, go to qring.eu").language(Say.Language.EN_US).build());
//                    voiceResponse = voiceResponseBuilder.dial(dialBuilder.build()).build();
//                } else {
//                    // no member is found
//                    log.error("no member is found in group");
//                    voiceResponse = voiceResponseBuilder.say(new Say.Builder("Thanks for calling to " + groupDO.getName() + " but no member is found").build()).build();
//                }
//            } else {
//                log.error("group is found");
//                // some internal issue occurred
//                voiceResponse = voiceResponseBuilder.say(new Say.Builder("Group is not found, Please scan a valid QR code which is attached with some group").build()).build();
//            }
//        } else {
//            log.info("retrieved from redis failed for number {} and code {}", twilioCallRequestDTO.getFrom(), twilioCallRequestDTO.getDigits());
//            voiceResponse = voiceResponseBuilder.say(new Say.Builder("Please scan valid QRing QR code to initiate the group call").build()).build();
//        }
//        log.info("going to connect all members {}", voiceResponse == null ? voiceResponseBuilder.say(new Say.Builder("Special flow error occurred").build()).build().toXml() : voiceResponse.toXml());
//        return ResponseEntity.ok(voiceResponse == null ? voiceResponseBuilder.say(new Say.Builder("Special flow error occurred").build()).build().toXml() : voiceResponse.toXml());
    }

}
