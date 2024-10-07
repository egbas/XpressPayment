package com.egbas.XpressPayment.service.impl;

import com.egbas.XpressPayment.dto.request.PaymentRequest;
import com.egbas.XpressPayment.dto.response.PaymentResponse;
import com.egbas.XpressPayment.service.AirtimePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class AirtimePaymentServiceImpl implements AirtimePaymentService {
// These keys are usually gotten upon registration and set in the app.prop file
    // I couldn't register so the keys are missing
    // The API integration would not work now but i think the logic is alright if the keys are available
    private static final String PHONE_NUMBER_REGEX = "^0?[0-9]{11,14}$";
    private final String url;
    private final String privateKey;
    private final String publicKey;
    private final RestTemplate restTemplate;

    public AirtimePaymentServiceImpl(@Value("${payment.api.url}") String url,
                              @Value("${payment.private.key}") String privateKey,
                              @Value("${payment.public.key}") String publicKey,
                              RestTemplate restTemplate) {
        this.url = url;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.restTemplate = restTemplate;
    }


    public ResponseEntity<PaymentResponse> initiateAirtimePurchase(PaymentRequest request){

        //Payment is initiated as phone number is first verified to be valid
        try{

            if (!isValidPhoneNumber(request.getDetails().getPhoneNumber())) {
                log.error("Invalid phone number: " + request.getDetails().getPhoneNumber());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            String requestJson = objectMapper.writeValueAsString(request);

            String paymentHash = calculateHMAC512(requestJson, privateKey);

            // airtime purchase is done here
            return buyAirtime(request, paymentHash);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<PaymentResponse> buyAirtime(PaymentRequest requestBody, String paymentHash ){

        // This method makes the http request and purchases airtime if all headers and requests are properly set
        try{

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + publicKey);
            headers.set("PaymentHash", paymentHash);
            headers.set("channel", "API");

            log.info("payment hash: " + paymentHash);

            HttpEntity<PaymentRequest> request = new HttpEntity<>(requestBody, headers);


            ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(url, request, PaymentResponse.class);
            PaymentResponse response = responseEntity.getBody();

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("Payment successful. Response: " + responseEntity.getBody());
                return ResponseEntity.ok(responseEntity.getBody());
            } else {
                log.info("Payment failed. Response: " + responseEntity.getBody());
                return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody == null) {
                log.info("HTTP error occurred, but no response body was provided.");
            } else {
                log.info("HTTP error. Response: " + responseBody);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public static String calculateHMAC512(String data, String key) {
        // Hashing algorithm for the payment hash
        String HMAC_SHA512 = "HmacSHA512";
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA512);
        Mac mac = null;

        try {
            mac = Mac.getInstance(HMAC_SHA512);
            mac.init(secretKeySpec);
            return String.valueOf(Hex.encode(mac.doFinal(data.getBytes())));

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.trim().matches(PHONE_NUMBER_REGEX);
    }

}
