package com.assettrack.allocation.controller;

import com.assettrack.allocation.config.SecurityConfig;
import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.exception.*;
import com.assettrack.allocation.repository.UserRepository;
import com.assettrack.allocation.security.JwtService;
import com.assettrack.allocation.service.AllocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AllocationController.class)
@Import(SecurityConfig.class)
class AllocationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AllocationService allocationService;
    @MockBean UserRepository userRepository;
    @MockBean JwtService jwtService;

    // ── ASSIGN ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ADMIN can assign asset → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void assignAsset_asAdmin_returns200() throws Exception {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(1L);
        req.setUserId(2L);

        when(allocationService.assignAsset(any(), any()))
                .thenReturn(new MessageResponse("Asset assigned successfully"));

        mockMvc.perform(post("/api/allocations/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset assigned successfully"));
    }

    @Test
    @DisplayName("DEVELOPER cannot assign → 403 Forbidden")
    @WithMockUser(roles = "DEVELOPER")
    void assignAsset_asDeveloper_returns403() throws Exception {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(1L);
        req.setUserId(2L);

        mockMvc.perform(post("/api/allocations/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Assign missing assetId → 400 validation error")
    @WithMockUser(roles = "ADMIN")
    void assignAsset_missingAssetId_returns400() throws Exception {
        // assetId intentionally missing
        String body = """
                { "userId": 2 }
                """;

        mockMvc.perform(post("/api/allocations/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── RETURN ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("MANAGER can return asset → 200 OK")
    @WithMockUser(roles = "MANAGER")
    void returnAsset_asManager_returns200() throws Exception {
        when(allocationService.returnAsset(1L))
                .thenReturn(new MessageResponse("Asset returned successfully"));

        mockMvc.perform(post("/api/allocations/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset returned successfully"));
    }

    @Test
    @DisplayName("Return non-existent allocation → 404")
    @WithMockUser(roles = "ADMIN")
    void returnAsset_notFound_returns404() throws Exception {
        when(allocationService.returnAsset(999L))
                .thenThrow(new ResourceNotFoundException("Allocation not found with id: 999"));

        mockMvc.perform(post("/api/allocations/999/return"))
                .andExpect(status().isNotFound());
    }

    // ── TRANSFER ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ADMIN can transfer asset → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void transferAsset_asAdmin_returns200() throws Exception {
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(1L);
        req.setNewUserId(5L);

        when(allocationService.transferAsset(any(), any()))
                .thenReturn(new MessageResponse("Asset transferred successfully"));

        mockMvc.perform(post("/api/allocations/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset transferred successfully"));
    }

    // ── HISTORY ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DEVELOPER can read history → 200 OK")
    @WithMockUser(roles = "DEVELOPER")
    void getHistory_asDeveloper_returns200() throws Exception {
        when(allocationService.getAllocationHistory(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/allocations/asset/1/history"))
                .andExpect(status().isOk());
    }

    // ── SPARE LAPTOPS ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Get spare laptops with filters → 200 OK")
    @WithMockUser(roles = "DEVELOPER")
    void getSpareLaptops_withFilters_returns200() throws Exception {
        when(allocationService.getSpareLaptops("Dell", 16, 512)).thenReturn(List.of());

        mockMvc.perform(get("/api/allocations/spare-laptops")
                        .param("brand", "Dell")
                        .param("ram", "16")
                        .param("storage", "512"))
                .andExpect(status().isOk());
    }
}
