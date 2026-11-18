package com.plasticexchange.controller;

import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import com.plasticexchange.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AgentRepository agents;
    private final PickupRepository pickups;
    private final UserRepository users;
    private final RateRepository rates;
    private final RewardRepository rewards;
    private final PickupService pickupService;
    private final AgentAssignmentService assignment;

    public AdminController(AgentRepository agents, PickupRepository pickups, UserRepository users, RateRepository rates,
                           RewardRepository rewards, PickupService pickupService, AgentAssignmentService assignment) {
        this.agents = agents; this.pickups = pickups; this.users = users; this.rates = rates;
        this.rewards = rewards; this.pickupService = pickupService; this.assignment = assignment;
    }

    @GetMapping("/agents")
    public List<Agent> agents() { return agents.findAll(); }

    @PutMapping("/agents/{id}/approve")
    public Agent approve(@PathVariable String id) {
        Agent a = agents.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agent not found"));
        a.setApproved(true);
        agents.save(a);
        assignment.assignPending();
        return a;
    }

    @GetMapping("/pickups")
    public List<Pickup> pickups() { return pickups.findAllByOrderByCreatedAtDesc(); }

    @PutMapping("/pickups/{id}/review")
    public Pickup review(@PathVariable String id, @RequestParam boolean approve, @RequestParam(required = false) String type) {
        return pickupService.reviewManually(id, approve, type);
    }

    @GetMapping("/rates")
    public List<RewardRate> rates() { return rates.findAll(); }

    @PutMapping("/rates/{type}")
    public RewardRate setRate(@PathVariable String type, @RequestBody Map<String, Integer> body) {
        return rates.save(new RewardRate(type.toUpperCase(), body.getOrDefault("pointsPerKg", 0)));
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<Pickup> all = pickups.findAll();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("users", users.findAll().stream().filter(u -> u.getRole() == Role.USER).count());
        m.put("agents", agents.count());
        m.put("pickups", all.size());
        m.put("pendingReview", all.stream().filter(p -> p.getStatus() == PickupStatus.REQUESTED).count());
        m.put("totalKg", all.stream().filter(p -> p.getStatus() == PickupStatus.COLLECTED || p.getStatus() == PickupStatus.COMPLETED)
                .mapToDouble(Pickup::getWeightKg).sum());
        m.put("pointsIssued", rewards.findAll().stream().mapToInt(RewardTxn::getPoints).sum());
        return m;
    }
}
