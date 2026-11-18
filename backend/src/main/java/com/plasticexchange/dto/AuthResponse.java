package com.plasticexchange.dto;

import com.plasticexchange.model.Role;

public record AuthResponse(String token, String id, String name, String email, Role role, int rewardPoints) { }
