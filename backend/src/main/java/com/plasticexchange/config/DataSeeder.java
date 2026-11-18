package com.plasticexchange.config;

import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Map;

/** Creates the default admin account and reward rates on first start. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final RateRepository rates;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, RateRepository rates, PasswordEncoder encoder) {
        this.users = users; this.rates = rates; this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail("admin@plastic.com")) {
            User a = new User();
            a.setName("Admin");
            a.setEmail("admin@plastic.com");
            a.setPasswordHash(encoder.encode("admin123"));
            a.setRole(Role.ADMIN);
            users.save(a);
            System.out.println(">> Default admin created: admin@plastic.com / admin123");
        }
        if (rates.count() == 0) {
            Map.of("PET", 10, "HDPE", 8, "PP", 6, "LDPE", 5, "PS", 4, "PVC", 3, "OTHER", 2)
                .forEach((t, p) -> rates.save(new RewardRate(t, p)));
        }
    }
}
