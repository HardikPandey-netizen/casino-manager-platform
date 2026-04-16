package com.hardik.casino.DTOs;

public record LoginResponse(
        Long id,
        String username,
        String role,
        String token,
        String message
) {
}
