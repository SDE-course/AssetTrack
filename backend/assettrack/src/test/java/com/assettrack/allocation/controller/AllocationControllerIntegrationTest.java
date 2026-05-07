package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.exception.*;
import com.assettrack.allocation.service.AllocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AllocationController.class)
class AllocationControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  AllocationService allocationService;

    // ── Assign ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void assignAsset_returns200() throws Exception {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(1L); req.setUserId(2L);

        when(allocationService.assignAsset(any(), anyLong()))
                .thenReturn(new MessageResponse("Asset assigned successfully"));

        mockMvc.perform(post("/api/allocations/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset assigned successfully"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void assignAsset_alreadyAssigned_returns409() throws Exception {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(1L); req.setUserId(2L);

        when(allocationService.assignAsset(any(), anyLong()))
                .thenThrow(new AssetAlreadyAssignedException("Asset already has an active allocation."));

        mockMvc.perform(post("/api/allocations/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "1", roles = {"DEVELOPER"})
    void assignAsset_developerForbidden_returns403() throws Exception {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(1L); req.setUserId(2L);

        mockMvc.perform(post("/api/allocations/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ── Return ────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void returnAsset_returns200() throws Exception {
        when(allocationService.returnAsset(100L))
                .thenReturn(new MessageResponse("Asset returned successfully"));

        mockMvc.perform(post("/api/allocations/100/return").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset returned successfully"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void returnAsset_notFound_returns404() throws Exception {
        when(allocationService.returnAsset(999L))
                .thenThrow(new ResourceNotFoundException("Allocation not found with id: 999"));

        mockMvc.perform(post("/api/allocations/999/return").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void transferAsset_returns200() throws Exception {
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L); req.setNewUserId(5L);

        when(allocationService.transferAsset(any(), anyLong()))
                .thenReturn(new MessageResponse("Asset transferred successfully"));

        mockMvc.perform(post("/api/allocations/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset transferred successfully"));
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = {"DEVELOPER"})
    void getHistory_returns200() throws Exception {
        AllocationHistoryResponse hist = AllocationHistoryResponse.builder()
                .user("Ahmed").assignedDate(LocalDateTime.now()).active(false).build();

        when(allocationService.getAllocationHistory(10L)).thenReturn(List.of(hist));

        mockMvc.perform(get("/api/allocations/asset/10/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user").value("Ahmed"));
    }

    // ── Spare Laptops ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = {"DEVELOPER"})
    void getSpareLaptops_returns200() throws Exception {
        SpareLaptopResponse laptop = SpareLaptopResponse.builder()
                .id(1L).name("MacBook").brand("Apple").ram(16).storage(512).status("AVAILABLE").build();

        when(allocationService.getSpareLaptops(null, null, null)).thenReturn(List.of(laptop));

        mockMvc.perform(get("/api/allocations/spare-laptops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Apple"));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void getSpareLaptops_withFilters_returns200() throws Exception {
        when(allocationService.getSpareLaptops("Apple", 16, 512)).thenReturn(List.of());

        mockMvc.perform(get("/api/allocations/spare-laptops")
                        .param("brand", "Apple")
                        .param("ram", "16")
                        .param("storage", "512"))
                .andExpect(status().isOk());
    }
}
