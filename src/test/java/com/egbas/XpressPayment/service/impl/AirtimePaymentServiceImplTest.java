package com.egbas.XpressPayment.service.impl;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.egbas.XpressPayment.dto.request.PaymentRequest;
import com.egbas.XpressPayment.dto.request.UserDetail;
import com.egbas.XpressPayment.dto.response.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringJUnitConfig

class AirtimePaymentServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AirtimePaymentServiceImpl airtimePaymentService;

    @Value("${payment.api.url}")
    private String apiUrl = "http://localhost/api/payment";

    @Value("${payment.private.key}")
    private String privateKey = "privateTestKey";

    @Value("${payment.public.key}")
    private String publicKey = "publicTestKey";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        airtimePaymentService = new AirtimePaymentServiceImpl(apiUrl, privateKey, publicKey, restTemplate);
    }

    @Test
    void testInitiateAirtimePurchase_Success() {
        PaymentRequest paymentRequest = new PaymentRequest();
        UserDetail details = new UserDetail();
        details.setPhoneNumber("08123456789");  // Valid phone number
        paymentRequest.setDetails(details);

        PaymentResponse paymentResponse = new PaymentResponse();
        ResponseEntity<PaymentResponse> mockResponseEntity = new ResponseEntity<>(paymentResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponseEntity);

        ResponseEntity<PaymentResponse> response = airtimePaymentService.initiateAirtimePurchase(paymentRequest);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testInitiateAirtimePurchase_InvalidPhoneNumber() {
        PaymentRequest paymentRequest = new PaymentRequest();
        UserDetail details = new UserDetail();
        details.setPhoneNumber("12345");  // Invalid phone number
        paymentRequest.setDetails(details);

        ResponseEntity<PaymentResponse> response = airtimePaymentService.initiateAirtimePurchase(paymentRequest);

        assertNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testBuyAirtime_Success() {
        PaymentRequest paymentRequest = new PaymentRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + publicKey);
        headers.set("PaymentHash", "mockHash");

        PaymentResponse paymentResponse = new PaymentResponse();
        ResponseEntity<PaymentResponse> mockResponseEntity = new ResponseEntity<>(paymentResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenReturn(mockResponseEntity);

        ResponseEntity<PaymentResponse> response = airtimePaymentService.buyAirtime(paymentRequest, "mockHash");

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testBuyAirtime_Failure() {
        PaymentRequest mockRequest = new PaymentRequest();

        // Assuming PaymentRequest has a setDetails method or similar, you need to initialize it as required
        UserDetail details = new UserDetail();
        details.setPhoneNumber("08123456789"); // Example phone number
        mockRequest.setDetails(details);
        // Mock the RestTemplate to simulate an HTTP error with no response body
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        // Simulate the RestTemplate throwing an exception
        Mockito.when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(PaymentResponse.class)))
                .thenThrow(exception);

        // Call the method and assert the expected behavior
        ResponseEntity<PaymentResponse> response = airtimePaymentService.buyAirtime(mockRequest, "dummyPaymentHash");

        // Validate that the response is INTERNAL_SERVER_ERROR and has no body
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCalculateHMAC512() {
        String data = "{\"amount\":\"100\"}";
        String key = "secretKey";

        String hash = AirtimePaymentServiceImpl.calculateHMAC512(data, key);

        assertNotNull(hash);
        assertTrue(hash.length() > 0);  // Ensure hash is generated
    }

    @Test
    public void testIsValidPhoneNumber() {

        // Testing invalid phone numbers
        assertFalse(AirtimePaymentServiceImpl.isValidPhoneNumber("0812345678"));   // Invalid - less than 11 digits
        assertFalse(AirtimePaymentServiceImpl.isValidPhoneNumber("123456789"));    // Invalid - less than 11 digits
        assertFalse(AirtimePaymentServiceImpl.isValidPhoneNumber("abcdefghijk"));  // Invalid - non-numeric
        assertFalse(AirtimePaymentServiceImpl.isValidPhoneNumber("0801234567890123")); // Invalid - more than 14 digits

        // Testing valid phone numbers
        assertTrue(AirtimePaymentServiceImpl.isValidPhoneNumber("08123456789"));       // Valid - 11 digits with leading '0'
        assertTrue(AirtimePaymentServiceImpl.isValidPhoneNumber("2348123456789"));     // Valid - 13 digits
        assertTrue(AirtimePaymentServiceImpl.isValidPhoneNumber("0801234567890"));     // Valid - 12 digits with leading '0'
        assertTrue(AirtimePaymentServiceImpl.isValidPhoneNumber("12345678901"));       // Valid - 11 digits without leading '0'
    }
}

