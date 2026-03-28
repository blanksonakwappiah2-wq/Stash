package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.PermissionRequest;
import com.quickbite.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionRequest, Long> {
    List<PermissionRequest> findByAgent(User agent);
}
