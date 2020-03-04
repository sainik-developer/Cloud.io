package com.cloudio.rest.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CompanySettingValidator.class)
public @interface ValidCompanySetting {
    String message() default "Invalid Company Setting, ringType ALL_AT_ONCE should not have ringOrderAccountIds and orderDelayInSec or ringType IN_ORDER should have ringOrderAccountIds and orderDelayInSec";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
