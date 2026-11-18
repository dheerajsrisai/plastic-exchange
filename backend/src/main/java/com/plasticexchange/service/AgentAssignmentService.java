package com.plasticexchange.service;

import com.plasticexchange.model.*;
import com.plasticexchange.repository.*;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class AgentAssignmentService {
    private final AgentRepository agents;
    private final PickupRepository pickups;

    public AgentAssignmentService(AgentRepository agents, PickupRepository pickups) {
        this.agents = agents; this.pickups = pickups;
    }

    /** Picks the available, approved agent in the same area with the fewest active pickups
     *  (falls back to any available agent if none in that area). Returns true if assigned. */
    public boolean tryAssign(Pickup p) {
        List<Agent> available = agents.findByApprovedTrueAndAvailableTrue();
        List<Agent> sameArea = available.stream()
                .filter(a -> a.getArea() != null && a.getArea().equalsIgnoreCase(p.getArea())).toList();
        List<Agent> pool = sameArea.isEmpty() ? available : sameArea;
        return pool.stream()
                .min(Comparator.comparingLong(a -> pickups.countByAgentIdAndStatus(a.getId(), PickupStatus.ASSIGNED)))
                .map(a -> {
                    p.setAgentId(a.getId());
                    p.setAgentName(a.getName());
                    p.setStatus(PickupStatus.ASSIGNED);
                    pickups.save(p);
                    return true;
                }).orElse(false);
    }

    /** Assign every SCHEDULED pickup that is still waiting for an agent. */
    public void assignPending() {
        for (Pickup p : pickups.findByStatus(PickupStatus.SCHEDULED)) tryAssign(p);
    }
}
