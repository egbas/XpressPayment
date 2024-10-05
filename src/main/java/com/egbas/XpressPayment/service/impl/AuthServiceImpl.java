package com.egbas.XpressPayment.service.impl;

import com.egbas.XpressPayment.config.JwtService;
import com.egbas.XpressPayment.dto.request.LoginRequest;
import com.egbas.XpressPayment.dto.request.RegisterRequest;
import com.egbas.XpressPayment.dto.response.ApiResponse;
import com.egbas.XpressPayment.entity.User;
import com.egbas.XpressPayment.repository.UserRepository;
import com.egbas.XpressPayment.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public ApiResponse<?> register(RegisterRequest registerRequest) {
        // Perform email validation
        if (!isValidEmail(registerRequest.getEmail())) {
            return ApiResponse.builder()
                    .status("error")
                    .message("Invalid email format")
                    .statusCode(HttpStatus.BAD_REQUEST)
                    .build();
        }
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            return ApiResponse.builder()
                    .status("failure")
                    .message("User already exists")
                    .statusCode(HttpStatus.CONFLICT)
                    .build();
        }
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        userRepository.save(user);

        return ApiResponse.builder()
                .status("success")
                .message("Registration successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @Override
    public ApiResponse<?> login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword())
        );

        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail());
        }

        User savedUser = optionalUser.get();
        String jwtToken = jwtService.generateToken(savedUser);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", jwtToken);

        return ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .message("Login successful")
                .data(data)
                .statusCode(HttpStatus.OK)
                .build();
    }

    private boolean isValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}

