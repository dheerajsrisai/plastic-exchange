package com.plasticexchange.service;

import com.plasticexchange.dto.AiResult;
import com.plasticexchange.exception.ApiException;
import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class PickupService {
    private static final Set<String> EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final PickupRepository pickups;
    private final AiVerificationService ai;
    private final AgentAssignmentService assignment;
    private final RewardService rewards;
    private final UserRepository users;

    @Value("${app.upload-dir}") private String uploadDir;
    @Value("${ai.confidence-threshold}") private double threshold;

    public PickupService(PickupRepository pickups, AiVerificationService ai, AgentAssignmentService assignment,
                         RewardService rewards, UserRepository users) {
        this.pickups = pickups; this.ai = ai; this.assignment = assignment; this.rewards = rewards; this.users = users;
    }

    public Pickup create(User user, MultipartFile image, String declaredType, double estWeight,
                         String address, String area, LocalDateTime scheduledAt) {
        if (image == null || image.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Please upload a photo of the plastic");
        try {
            String ext = org.springframework.util.StringUtils.getFilenameExtension(image.getOriginalFilename());
            ext = ext != null && EXT.contains(ext.toLowerCase()) ? ext.toLowerCase() : "jpg";
            String fileName = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            byte[] bytes = image.getBytes();
            Files.write(dir.resolve(fileName), bytes);

            Pickup p = new Pickup();
            p.setUserId(user.getId());
            p.setUserName(user.getName());
            p.setImageUrl("/uploads/" + fileName);
            p.setEstimatedWeightKg(estWeight);
            p.setAddress(address != null && !address.isBlank() ? address : user.getAddress());
            p.setArea(area != null && !area.isBlank() ? area : user.getArea());
            p.setScheduledAt(scheduledAt != null ? scheduledAt : LocalDateTime.now().plusDays(1));
            p.setPlasticType(declaredType == null ? "OTHER" : declaredType.toUpperCase());

            AiResult r = ai.verify(bytes, fileName);
            p.setAiType(r.type());
            p.setAiConfidence(r.confidence());
            boolean plastic = !r.type().equals("NON_PLASTIC") && !r.type().equals("UNKNOWN");
            if (plastic && r.confidence() >= threshold) {
                p.setPlasticType(r.type());
                p.setStatus(PickupStatus.VERIFIED);
            } else {
                p.setNeedsReview(true);
                p.setStatus(PickupStatus.REQUESTED);
            }
            pickups.save(p);
            if (p.getStatus() == PickupStatus.VERIFIED) schedule(p);
            return p;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the image");
        }
    }

    /** VERIFIED -> SCHEDULED -> (ASSIGNED if an agent is free). */
    public void schedule(Pickup p) {
        p.setStatus(PickupStatus.SCHEDULED);
        pickups.save(p);
        assignment.tryAssign(p);
    }

    public Pickup reviewManually(String id, boolean approve, String type) {
        Pickup p = pickups.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pickup not found"));
        if (p.getStatus() != PickupStatus.REQUESTED)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only pickups awaiting review can be reviewed");
        p.setNeedsReview(false);
        if (!approve) { p.setStatus(PickupStatus.REJECTED); return pickups.save(p); }
        if (type != null && !type.isBlank()) p.setPlasticType(type.toUpperCase());
        p.setStatus(PickupStatus.VERIFIED);
        schedule(p);
        return p;
    }

    public Pickup collect(Agent agent, String id, double weightKg, String confirmedType) {
        Pickup p = owned(agent, id);
        if (p.getStatus() != PickupStatus.ASSIGNED) throw new ApiException(HttpStatus.BAD_REQUEST, "Pickup is not in ASSIGNED state");
        if (confirmedType != null && !confirmedType.isBlank()) p.setPlasticType(confirmedType.toUpperCase());
        int points = rewards.pointsFor(p.getPlasticType(), weightKg);
        p.setWeightKg(weightKg);
        p.setPointsAwarded(points);
        p.setStatus(PickupStatus.COLLECTED);
        pickups.save(p);
        User u = users.findById(p.getUserId()).orElseThrow();
        rewards.award(u, points, "Recycled " + weightKg + " kg " + p.getPlasticType());
        return p;
    }

    public Pickup complete(Agent agent, String id) {
        Pickup p = owned(agent, id);
        if (p.getStatus() != PickupStatus.COLLECTED) throw new ApiException(HttpStatus.BAD_REQUEST, "Collect the plastic first");
        p.setStatus(PickupStatus.COMPLETED);
        pickups.save(p);
        assignment.assignPending();
        return p;
    }

    private Pickup owned(Agent agent, String id) {
        Pickup p = pickups.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pickup not found"));
        if (!agent.getId().equals(p.getAgentId())) throw new ApiException(HttpStatus.FORBIDDEN, "This pickup is not assigned to you");
        return p;
    }
}
