package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.AllocationHistoryResponse;
import com.assettrack.allocation.dto.AssignAssetRequest;
import com.assettrack.allocation.dto.MessageResponse;
import com.assettrack.allocation.dto.SpareLaptopResponse;
import com.assettrack.allocation.dto.TransferAssetRequest;
import com.assettrack.allocation.service.AllocationService;
import com.assettrack.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AllocationController.class)
@Import(SecurityConfig.class)
class AllocationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AllocationService allocationService;

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void assignAsset_returnsSuccess() throws Exception {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(1L);
        request.setUserId(3L);
        request.setNotes("Assigned for backend development");

        when(allocationService.assignAsset(any(AssignAssetRequest.class), anyLong()))
                .thenReturn(new MessageResponse("Asset assigned successfully"));

        mockMvc.perform(post("/api/allocations/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset assigned successfully"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"DEVELOPER"})
    void assignAsset_developerForbidden() throws Exception {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(1L);
        request.setUserId(3L);

        mockMvc.perform(post("/api/allocations/assign")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void returnAsset_returnsSuccess() throws Exception {
        when(allocationService.returnAsset(10L))
                .thenReturn(new MessageResponse("Asset returned successfully"));

        mockMvc.perform(post("/api/allocations/10/return").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset returned successfully"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"MANAGER"})
    void transferAsset_returnsSuccess() throws Exception {
        TransferAssetRequest request = new TransferAssetRequest();
        request.setAllocationId(10L);
        request.setNewUserId(5L);
        request.setNotes("Transferred to frontend developer");

        when(allocationService.transferAsset(any(TransferAssetRequest.class), anyLong()))
                .thenReturn(new MessageResponse("Asset transferred successfully"));

        mockMvc.perform(post("/api/allocations/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset transferred successfully"));
    }

    @Test
    @WithMockUser(roles = {"DEVELOPER"})
    void getHistory_returnsHistory() throws Exception {
        AllocationHistoryResponse historyResponse = AllocationHistoryResponse.builder()
                .allocationId(20L)
                .user("Ahmed")
                .userId(3L)
                .assignedBy("Admin")
                .active(false)
                .build();

        when(allocationService.getAllocationHistory(7L)).thenReturn(List.of(historyResponse));

        mockMvc.perform(get("/api/allocations/asset/7/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user").value("Ahmed"));
    }

    @Test
    @WithMockUser(roles = {"DEVELOPER"})
    void getCurrentOwner_returnsAvailableMessage() throws Exception {
        when(allocationService.getCurrentOwner(7L))
                .thenReturn(new MessageResponse("Asset is currently available"));

        mockMvc.perform(get("/api/allocations/current-owner/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Asset is currently available"));
    }

    @Test
    @WithMockUser(roles = {"DEVELOPER"})
    void getSpareLaptops_returnsList() throws Exception {
        SpareLaptopResponse response = SpareLaptopResponse.builder()
                .id(1L)
                .name("MacBook Pro")
                .serialNumber("SN-001")
                .brand("Apple")
                .ram(16)
                .storage(512)
                .status("AVAILABLE")
                .build();

        when(allocationService.getSpareLaptops(null, null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/allocations/spare-laptops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Apple"));
    }
}