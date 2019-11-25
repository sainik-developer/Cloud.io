package com.cloudio.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AskfastAdapterRequestDTO {
    private String address;
    private String url;
    private AdapterType adapterType = AdapterType.SMS;
    private final String senderName = "Cloudio";

    @AllArgsConstructor
    public static enum AdapterType {

        SMS("SMS"),
        EMAIL("EMAIL");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }
}
