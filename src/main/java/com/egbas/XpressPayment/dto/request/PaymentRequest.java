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
public class PaymentRequest {

    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("uniqueCode")
    private String uniqueCode;
    @JsonProperty("details")
    private UserDetail details;


}
