package com.plasticexchange.controller;

import com.plasticexchange.dto.RedeemRequest;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import com.plasticexchange.service.RewardService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    private final UserRepository users;
    private final RewardRepository rewards;
    private final RedemptionRepository redemptions;
    private final RewardService service;

    public RewardController(UserRepository users, RewardRepository rewards,
                            RedemptionRepository redemptions, RewardService service) {
        this.users = users; this.rewards = rewards; this.redemptions = redemptions; this.service = service;
    }

    @GetMapping("/balance")
    public Map<String, Object> balance(Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        return Map.of("points", u.getRewardPoints(),
                "earnings", rewards.findByUserIdOrderByCreatedAtDesc(u.getId()),
                "redemptions", redemptions.findByUserIdOrderByCreatedAtDesc(u.getId()));
    }

    @GetMapping("/catalog")
    public List<RewardService.CatalogItem> catalog() { return RewardService.CATALOG; }

    @PostMapping("/redeem")
    public Map<String, Object> redeem(Authentication auth, @Valid @RequestBody RedeemRequest r) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        service.redeem(u, r.item());
        return Map.of("points", u.getRewardPoints(), "message", "Redeemed: " + r.item());
    }
}
