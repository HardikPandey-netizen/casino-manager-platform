package com.hardik.casino.Controllers;

import com.hardik.casino.DTOs.ForgotPasswordRequest;
import com.hardik.casino.DTOs.LoginRequest;
import com.hardik.casino.DTOs.LoginResponse;
import com.hardik.casino.DTOs.MessageResponse;
import com.hardik.casino.DTOs.RegisterRequest;
import com.hardik.casino.DTOs.RegisterResponse;
import com.hardik.casino.DTOs.ResetPasswordRequest;
import com.hardik.casino.Entities.User;
import com.hardik.casino.Security.JwtService;
import com.hardik.casino.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/csp/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        User user = userService.register(
                registerRequest.username(),
                registerRequest.email(),
                registerRequest.password()
        );
        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                "User registered successfully"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(
                loginRequest.email(),
                loginRequest.username(),
                loginRequest.password()
        );
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .disabled(!user.isEnabled())
                .build();
        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                jwtService.generateToken(userDetails),
                "Login successful"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        userService.forgotPassword(forgotPasswordRequest.email());
        return ResponseEntity.ok(new MessageResponse("If the email exists, a password reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @RequestParam("token") String token,
            @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        userService.resetPassword(token, resetPasswordRequest.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password reset successful"));
    }
}
