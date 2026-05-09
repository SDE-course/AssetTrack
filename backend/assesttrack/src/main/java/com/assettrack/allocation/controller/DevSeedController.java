package com.assettrack.allocation.controller;

import com.assettrack.allocation.entity.*;
import com.assettrack.allocation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DEV-ONLY endpoint — seeds the database with sample users and assets
 * so you can test the allocation workflow without manual SQL.
 *
 * Usage:  POST /api/dev/seed
 *
 * ⚠️  Remove (or restrict with @Profile("dev")) before going to production.
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevSeedController {

    private final UserRepository  userRepository;
    private final AssetRepository assetRepository;

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {

        // ── Users ────────────────────────────────────────────────────────────
        User admin = userRepository.findByEmail("admin@assettrack.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("System Admin")
                        .email("admin@assettrack.com")
                        .role(Role.ADMIN)
                        .build()));

        User manager = userRepository.findByEmail("manager@assettrack.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Alice Manager")
                        .email("manager@assettrack.com")
                        .role(Role.MANAGER)
                        .build()));

        User dev1 = userRepository.findByEmail("ahmed@assettrack.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Ahmed Developer")
                        .email("ahmed@assettrack.com")
                        .role(Role.DEVELOPER)
                        .build()));

        User dev2 = userRepository.findByEmail("sara@assettrack.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Sara Frontend")
                        .email("sara@assettrack.com")
                        .role(Role.DEVELOPER)
                        .build()));

        // ── Assets ───────────────────────────────────────────────────────────
        Asset laptop1 = assetRepository.findAll().stream()
                .filter(a -> "SN-DELL-001".equals(a.getSerialNumber()))
                .findFirst().orElseGet(() ->
                assetRepository.save(Asset.builder()
                        .name("Dell XPS 15")
                        .serialNumber("SN-DELL-001")
                        .brand("Dell")
                        .type("LAPTOP")
                        .status(AssetStatus.AVAILABLE)
                        .ram(16)
                        .storage(512)
                        .build()));

        Asset laptop2 = assetRepository.findAll().stream()
                .filter(a -> "SN-APPLE-002".equals(a.getSerialNumber()))
                .findFirst().orElseGet(() ->
                assetRepository.save(Asset.builder()
                        .name("MacBook Pro 14")
                        .serialNumber("SN-APPLE-002")
                        .brand("Apple")
                        .type("LAPTOP")
                        .status(AssetStatus.AVAILABLE)
                        .ram(32)
                        .storage(1024)
                        .build()));

        Asset mouse = assetRepository.findAll().stream()
                .filter(a -> "SN-MX-003".equals(a.getSerialNumber()))
                .findFirst().orElseGet(() ->
                assetRepository.save(Asset.builder()
                        .name("Logitech MX Master 3")
                        .serialNumber("SN-MX-003")
                        .brand("Logitech")
                        .type("MOUSE")
                        .status(AssetStatus.AVAILABLE)
                        .build()));

        Asset expiredAsset = assetRepository.findAll().stream()
                .filter(a -> "SN-OLD-004".equals(a.getSerialNumber()))
                .findFirst().orElseGet(() ->
                assetRepository.save(Asset.builder()
                        .name("Old Thinkpad (Expired)")
                        .serialNumber("SN-OLD-004")
                        .brand("Lenovo")
                        .type("LAPTOP")
                        .status(AssetStatus.EXPIRED)
                        .ram(8)
                        .storage(256)
                        .build()));

        return ResponseEntity.ok(Map.of(
                "message", "Seed data created successfully",
                "users", Map.of(
                        "admin_id",   admin.getId(),
                        "manager_id", manager.getId(),
                        "dev1_id",    dev1.getId(),
                        "dev2_id",    dev2.getId()
                ),
                "assets", Map.of(
                        "laptop1_id",     laptop1.getId(),
                        "laptop2_id",     laptop2.getId(),
                        "mouse_id",       mouse.getId(),
                        "expired_id",     expiredAsset.getId()
                ),
                "hint", "Use these IDs in your POST /api/allocations/assign requests"
        ));
    }
}
