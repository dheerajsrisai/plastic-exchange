package com.plasticexchange.dto;

import jakarta.validation.constraints.Positive;

public record CollectRequest(@Positive double weightKg, String plasticType) { }
