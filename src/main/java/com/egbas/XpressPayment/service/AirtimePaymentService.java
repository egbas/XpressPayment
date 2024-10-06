package com.egbas.XpressPayment.service;

import com.egbas.XpressPayment.dto.request.PaymentRequest;
import com.egbas.XpressPayment.dto.response.PaymentResponse;
import org.springframework.http.ResponseEntity;

public interface AirtimePaymentService {

    ResponseEntity<PaymentResponse> initiateAirtimePurchase(PaymentRequest request);
    ResponseEntity<PaymentResponse> buyAirtime(PaymentRequest request, String paymentHash);
}
