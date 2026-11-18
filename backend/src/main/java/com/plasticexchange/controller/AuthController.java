package com.plasticexchange.controller;

import com.plasticexchange.dto.*;
import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import com.plasticexchange.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final AgentRepository agents;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, AgentRepository agents, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.agents = agents; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        Role role = r.role() == Role.AGENT ? Role.AGENT : Role.USER;   // ADMIN cannot self-register
        User u = new User();
        u.setName(r.name());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setRole(role);
        u.setPhone(r.phone());
        u.setAddress(r.address());
        u.setArea(r.area());
        users.save(u);
        if (role == Role.AGENT) {
            Agent a = new Agent();
            a.setUserId(u.getId());
            a.setName(u.getName());
            a.setPhone(r.phone());
            a.setVehicleNo(r.vehicleNo());
            a.setArea(r.area());
            agents.save(a);   // approved=false until admin approves
        }
        return response(u);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return response(u);
    }

    private AuthResponse response(User u) {
        return new AuthResponse(jwt.generate(u.getEmail(), u.getRole().name()), u.getId(), u.getName(),
                u.getEmail(), u.getRole(), u.getRewardPoints());
    }
}
