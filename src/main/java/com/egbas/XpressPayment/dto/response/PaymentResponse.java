package com.egbas.XpressPayment.dto.response;

import com.egbas.XpressPayment.dto.request.UserDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("referenceId")
    private String referenceId;
    @JsonProperty("responseCode")
    private String responseCode;
    @JsonProperty("responseMessage")
    private String responseMessage;
    @JsonProperty("data")
    private UserDetail data;
}
