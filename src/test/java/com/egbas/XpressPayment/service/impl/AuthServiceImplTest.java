package com.egbas.XpressPayment.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import com.egbas.XpressPayment.config.JwtService;
import com.egbas.XpressPayment.dto.request.LoginRequest;
import com.egbas.XpressPayment.dto.request.RegisterRequest;
import com.egbas.XpressPayment.dto.response.ApiResponse;
import com.egbas.XpressPayment.entity.User;
import com.egbas.XpressPayment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthServiceImpl authService;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John", "Doe", "john.doe@example.com", "password123");
        loginRequest = new LoginRequest("john.doe@example.com", "password123");
    }

    @Test
    void register_success_test() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        ApiResponse<?> response = authService.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Registration successful", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void invalidEmail_test() {
        RegisterRequest invalidEmailRequest = new RegisterRequest("John", "Doe", "invalid-email", "password123");

        ApiResponse<?> response = authService.register(invalidEmailRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email format", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void userAlreadyExists_test() {
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new User()));

        ApiResponse<?> response = authService.register(registerRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_test() {
        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setPassword("encodedPassword");

        // Mock the behavior for user lookup
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        // Mock the authentication process
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));  // Return a mocked Authentication object

        // Mock the JWT token generation
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        ApiResponse<?> response = authService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwtToken", ((Map<String, Object>) response.getData()).get("accessToken"));
    }

    @Test
    void invalidCredentials_test() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));
    }
}
