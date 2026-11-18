package com.plasticexchange.controller;

import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import com.plasticexchange.service.PickupService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pickups")
public class PickupController {
    private final PickupService service;
    private final PickupRepository pickups;
    private final UserRepository users;

    public PickupController(PickupService service, PickupRepository pickups, UserRepository users) {
        this.service = service; this.pickups = pickups; this.users = users;
    }

    @PostMapping(consumes = "multipart/form-data")
    public Pickup create(Authentication auth,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam(defaultValue = "OTHER") String plasticType,
                         @RequestParam(defaultValue = "1") double estimatedWeightKg,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String area,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        return service.create(u, image, plasticType, estimatedWeightKg, address, area, scheduledAt);
    }

    @GetMapping("/my")
    public List<Pickup> mine(Authentication auth) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        return pickups.findByUserIdOrderByCreatedAtDesc(u.getId());
    }

    @GetMapping("/{id}")
    public Pickup one(Authentication auth, @PathVariable String id) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        Pickup p = pickups.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pickup not found"));
        if (!p.getUserId().equals(u.getId()) && u.getRole() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your pickup");
        return p;
    }

    @PutMapping("/{id}/schedule")
    public Pickup reschedule(Authentication auth, @PathVariable String id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt) {
        User u = users.findByEmail(auth.getName()).orElseThrow();
        Pickup p = pickups.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pickup not found"));
        if (!p.getUserId().equals(u.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Not your pickup");
        if (p.getStatus() == PickupStatus.COLLECTED || p.getStatus() == PickupStatus.COMPLETED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "This pickup is already collected");
        p.setScheduledAt(scheduledAt);
        return pickups.save(p);
    }
}
