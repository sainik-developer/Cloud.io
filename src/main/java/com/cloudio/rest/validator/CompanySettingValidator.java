package com.cloudio.rest.validator;

import com.cloudio.rest.dto.CompanySettingDTO;
import com.cloudio.rest.pojo.RingType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CompanySettingValidator implements ConstraintValidator<ValidCompanySetting, CompanySettingDTO> {
    @Override
    public boolean isValid(CompanySettingDTO companySettingDTO, ConstraintValidatorContext context) {
        if (companySettingDTO.getRingType() == RingType.IN_ORDER) {
            return companySettingDTO.getRingOrderAccountIds() != null
                    && companySettingDTO.getRingOrderAccountIds().size() > 0
                    && companySettingDTO.getOrderDelayInSec() > 5
                    && companySettingDTO.getOrderDelayInSec() < 30;
        }
        return true;
    }
}
