package com.hardik.casino.DTOs;

public record LoginRequest(
        String email,
        String username,
        String password
) {
}
