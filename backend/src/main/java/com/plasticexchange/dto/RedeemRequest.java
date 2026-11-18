package com.plasticexchange.dto;

import jakarta.validation.constraints.NotBlank;

public record RedeemRequest(@NotBlank String item) { }
