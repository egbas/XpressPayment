package com.egbas.XpressPayment.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetail {

    @JsonProperty("phoneNumber")
    private String phoneNumber;
    @JsonProperty("amount")
    private int amount;


}
