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

    // Regular expression for email validation
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // Compiled pattern for email validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Registers a new user by validating the provided registration details.
     *
     * @param registerRequest The registration request containing user details.
     * @return ApiResponse containing the status and message of the registration process.
     */
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

        // Check if a user with the given email already exists
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            return ApiResponse.builder()
                    .status("failure")
                    .message("User already exists")
                    .statusCode(HttpStatus.CONFLICT)
                    .build();
        }

        // Create a new User object with the provided registration details
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword())) // Encode the user's password
                .build();

        // Save the new user to the database
        userRepository.save(user);

        return ApiResponse.builder()
                .status("success")
                .message("Registration successful")
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    /**
     * Authenticates a user based on the provided login credentials.
     *
     * @param loginRequest The login request containing user email and password.
     * @return ApiResponse containing the status and JWT token upon successful login.
     * @throws UsernameNotFoundException if no user is found with the given email.
     */
    @Override
    public ApiResponse<?> login(LoginRequest loginRequest) {
        // Authenticate the user using the provided email and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword())
        );

        // Retrieve the user from the database
        Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail());
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail());
        }

        // Generate a JWT token for the authenticated user
        User savedUser = optionalUser.get();
        String jwtToken = jwtService.generateToken(savedUser);

        // Prepare the response data
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", jwtToken); // Include the JWT token in the response

        return ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .message("Login successful")
                .data(data)
                .statusCode(HttpStatus.OK)
                .build();
    }

    /**
     * Validates the format of the provided email address.
     * @param email The email address to validate.
     */
    private boolean isValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches(); // Check if the email matches the predefined regex pattern
    }
}

