package com.egbas.XpressPayment.service;

import com.egbas.XpressPayment.dto.request.LoginRequest;
import com.egbas.XpressPayment.dto.request.RegisterRequest;
import com.egbas.XpressPayment.dto.response.ApiResponse;

public interface AuthService {
    ApiResponse<?> register(RegisterRequest registerRequest);
    ApiResponse<?> login(LoginRequest loginRequest);
}
