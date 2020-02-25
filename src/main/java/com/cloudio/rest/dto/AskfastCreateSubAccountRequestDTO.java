package com.cloudio.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AskfastCreateSubAccountRequestDTO {

    private String userName;
    private String password;
    private String name;
    private List<ContactInfo> contactInfos;

    public enum ContactInfoTag {
        PHONE, EMAIL;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactInfo {
        private ContactInfoTag contactInfoTag;
        private String contactInfo;
    }
}
