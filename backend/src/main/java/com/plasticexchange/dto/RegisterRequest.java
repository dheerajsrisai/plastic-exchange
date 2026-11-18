package com.plasticexchange.dto;

import com.plasticexchange.model.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        Role role, String phone, String address, String area, String vehicleNo) { }
