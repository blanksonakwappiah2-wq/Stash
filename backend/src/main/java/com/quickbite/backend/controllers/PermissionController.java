package com.quickbite.backend.controllers;

import com.quickbite.backend.entities.PermissionRequest;
import com.quickbite.backend.entities.PermissionStatus;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import com.quickbite.backend.repositories.PermissionRepository;
import com.quickbite.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery/permissions")
public class PermissionController {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<PermissionRequest> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('DELIVERY_AGENT')")
    public ResponseEntity<?> submitPermission(@RequestBody PermissionRequest request) {
        if (request.getAgent() == null || request.getAgent().getId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Agent ID is required"));
        }
        User agent = userRepository.findById(request.getAgent().getId()).orElse(null);
        if (agent == null || agent.getRole() != UserRole.DELIVERY_AGENT) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid agent"));
        }
        request.setAgent(agent);
        request.setStatus(PermissionStatus.PENDING);
        return ResponseEntity.ok(permissionRepository.save(request));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> approvePermission(@PathVariable Long id) {
        PermissionRequest request = permissionRepository.findById(id).orElse(null);
        if (request == null) return ResponseEntity.notFound().build();
        request.setStatus(PermissionStatus.APPROVED);
        permissionRepository.save(request);
        return ResponseEntity.ok(Map.of("status", "APPROVED"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> rejectPermission(@PathVariable Long id) {
        PermissionRequest request = permissionRepository.findById(id).orElse(null);
        if (request == null) return ResponseEntity.notFound().build();
        request.setStatus(PermissionStatus.REJECTED);
        permissionRepository.save(request);
        return ResponseEntity.ok(Map.of("status", "REJECTED"));
    }
}
