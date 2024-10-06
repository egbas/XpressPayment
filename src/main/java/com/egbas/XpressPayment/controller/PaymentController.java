package com.egbas.XpressPayment.controller;

import com.egbas.XpressPayment.dto.request.PaymentRequest;
import com.egbas.XpressPayment.dto.response.PaymentResponse;
import com.egbas.XpressPayment.service.AirtimePaymentService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final AirtimePaymentService paymentService;


    @PostMapping("/airtime")
    public ResponseEntity<PaymentResponse> buyAirtime(@RequestBody PaymentRequest request) {
        try {

            ResponseEntity<PaymentResponse> response = paymentService.initiateAirtimePurchase(request);

            log.info("Response: " + response.getBody());
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

}
