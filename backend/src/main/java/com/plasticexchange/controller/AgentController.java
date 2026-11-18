package com.plasticexchange.controller;

import com.plasticexchange.dto.CollectRequest;
import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import com.plasticexchange.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final UserRepository users;
    private final AgentRepository agents;
    private final PickupRepository pickups;
    private final PickupService service;
    private final AgentAssignmentService assignment;

    public AgentController(UserRepository users, AgentRepository agents, PickupRepository pickups,
                           PickupService service, AgentAssignmentService assignment) {
        this.users = users; this.agents = agents; this.pickups = pickups; this.service = service; this.assignment = assignment;
    }

    private Agent me(Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        return agents.findByUserId(u.getId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agent profile not found"));
    }

    @GetMapping("/me")
    public Agent profile(Authentication auth) { return me(auth); }

    @PutMapping("/availability")
    public Agent availability(Authentication auth, @RequestBody Map<String, Boolean> body) {
        Agent a = me(auth);
        a.setAvailable(Boolean.TRUE.equals(body.get("available")));
        agents.save(a);
        if (a.isAvailable()) assignment.assignPending();
        return a;
    }

    @GetMapping("/pickups")
    public List<Pickup> myPickups(Authentication auth) {
        return pickups.findByAgentIdOrderByCreatedAtDesc(me(auth).getId());
    }

    @PutMapping("/pickups/{id}/collect")
    public Pickup collect(Authentication auth, @PathVariable String id, @Valid @RequestBody CollectRequest r) {
        return service.collect(me(auth), id, r.weightKg(), r.plasticType());
    }

    @PutMapping("/pickups/{id}/complete")
    public Pickup complete(Authentication auth, @PathVariable String id) {
        return service.complete(me(auth), id);
    }
}
