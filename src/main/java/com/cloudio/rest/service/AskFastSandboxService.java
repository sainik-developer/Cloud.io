package com.cloudio.rest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile(value = {"sandbox", "local"})
public class AskFastSandboxService implements AskFastService {
}
