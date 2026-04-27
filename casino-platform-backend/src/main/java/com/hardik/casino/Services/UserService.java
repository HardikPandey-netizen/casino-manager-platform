package com.hardik.casino.Services;

import com.hardik.casino.Entities.User;
import com.hardik.casino.Exceptions.InvalidResetTokenException;
import com.hardik.casino.Exceptions.UserAlreadyExistsException;
import com.hardik.casino.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.reset-password.expiration-minutes:30}")
    private long resetPasswordExpirationMinutes;

    @Value("${app.reset-password.url:http://localhost:3000/reset-password}")
    private String resetPasswordUrl;

    public User register(String username, String email, String password){
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(true);
        user.setAuthProvider("LOCAL");

        return userRepository.save(user);
    }

    public User login(String email, String username, String password) {
        String principal = resolvePrincipal(email, username);
        if (principal == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(principal, password)
        );

        User user = userRepository.findByEmail(principal);
        if (user == null) {
            user = userRepository.findByUsername(principal);
        }
        if (user == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        return user;
    }

    public void forgotPassword(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiresAt(LocalDateTime.now().plusMinutes(resetPasswordExpirationMinutes));
        userRepository.save(user);

        String resetLink = resetPasswordUrl + "?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        User user = userRepository.findByResetPasswordToken(token);
        if (user == null) {
            throw new InvalidResetTokenException("Invalid reset token");
        }
        if (user.getResetPasswordTokenExpiresAt() == null
                || user.getResetPasswordTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);
        userRepository.save(user);
    }

    private String resolvePrincipal(String email, String username) {
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase();
        }
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return null;
    }
}
