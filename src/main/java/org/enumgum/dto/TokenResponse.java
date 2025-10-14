package org.enumgum.dto;

public record TokenResponse(
    String accessToken, String refreshToken, String tokenType, int expiresIn) {}
