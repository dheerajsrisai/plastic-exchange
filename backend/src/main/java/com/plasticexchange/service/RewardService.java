package com.plasticexchange.service;

import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RewardService {
    public record CatalogItem(String name, int points) { }

    public static final List<CatalogItem> CATALOG = List.of(
            new CatalogItem("Plant sapling", 150),
            new CatalogItem("Reusable tote bag", 300),
            new CatalogItem("Mobile recharge ₹50", 500),
            new CatalogItem("Grocery voucher ₹100", 1000));

    private final RateRepository rates;
    private final RewardRepository rewards;
    private final RedemptionRepository redemptions;
    private final UserRepository users;

    public RewardService(RateRepository rates, RewardRepository rewards,
                         RedemptionRepository redemptions, UserRepository users) {
        this.rates = rates; this.rewards = rewards; this.redemptions = redemptions; this.users = users;
    }

    public int pointsFor(String type, double weightKg) {
        int rate = rates.findById(type == null ? "OTHER" : type.toUpperCase())
                .or(() -> rates.findById("OTHER")).map(RewardRate::getPointsPerKg).orElse(2);
        return (int) Math.round(weightKg * rate);
    }

    public void award(User user, int points, String reason) {
        user.setRewardPoints(user.getRewardPoints() + points);
        users.save(user);
        rewards.save(new RewardTxn(null, user.getId(), points, reason, java.time.LocalDateTime.now()));
    }

    public void redeem(User user, String itemName) {
        CatalogItem item = CATALOG.stream().filter(c -> c.name().equals(itemName)).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown reward item"));
        if (user.getRewardPoints() < item.points())
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough points. You need " + item.points() + ".");
        user.setRewardPoints(user.getRewardPoints() - item.points());
        users.save(user);
        redemptions.save(new Redemption(null, user.getId(), item.name(), item.points(), java.time.LocalDateTime.now()));
    }
}
