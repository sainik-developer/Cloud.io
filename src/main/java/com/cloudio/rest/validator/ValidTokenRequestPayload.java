package com.cloudio.rest.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TokenRequestValidator.class)
public @interface ValidTokenRequestPayload {
    String message() default "iOS should have Push token in token and VOIP token in voip field and Android should have fcm token in token field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
