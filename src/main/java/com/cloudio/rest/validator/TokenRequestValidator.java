package com.cloudio.rest.validator;

import com.cloudio.rest.dto.TokenDTO;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TokenRequestValidator implements ConstraintValidator<ValidTokenRequestPayload, TokenDTO> {
    @Override
    public boolean isValid(TokenDTO firebaseTokenDTO, ConstraintValidatorContext constraintValidatorContext) {
        return !StringUtils.isEmpty(firebaseTokenDTO.getToken()) &&
                (!firebaseTokenDTO.getDevice().equals("ios") || !StringUtils.isEmpty(firebaseTokenDTO.getVoipToken()));
    }
}
