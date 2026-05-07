package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.entity.*;
import com.assettrack.allocation.exception.*;
import com.assettrack.allocation.mapper.AllocationMapper;
import com.assettrack.allocation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {

    @Mock AllocationRepository allocationRepository;
    @Mock AssetRepository      assetRepository;
    @Mock UserRepository       userRepository;
    @Mock AllocationMapper     mapper;

    @InjectMocks AllocationServiceImpl service;

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private User admin;
    private User developer;
    private Asset availableAsset;
    private Asset assignedAsset;
    private Asset expiredAsset;
    private Allocation activeAllocation;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).name("Admin").email("admin@test.com").role("ADMIN").build();
        developer = User.builder().id(2L).name("Dev").email("dev@test.com").role("DEVELOPER").build();

        availableAsset = Asset.builder().id(10L).name("MacBook Pro").serialNumber("SN-001")
                .type("LAPTOP").status(AssetStatus.AVAILABLE).build();

        assignedAsset = Asset.builder().id(11L).name("Dell XPS").serialNumber("SN-002")
                .type("LAPTOP").status(AssetStatus.ASSIGNED).build();

        expiredAsset = Asset.builder().id(12L).name("Old HP").serialNumber("SN-003")
                .type("LAPTOP").status(AssetStatus.EXPIRED).build();

        activeAllocation = Allocation.builder()
                .id(100L).asset(assignedAsset).assignedTo(developer).assignedBy(admin)
                .assignedDate(LocalDateTime.now().minusDays(5)).active(true).build();
    }

    // ── 1. Assign Asset ──────────────────────────────────────────────────────

    @Test
    void assignAsset_success() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(10L); req.setUserId(2L); req.setNotes("test");

        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(allocationRepository.existsByAssetIdAndActiveTrue(10L)).thenReturn(false);

        MessageResponse response = service.assignAsset(req, 1L);

        assertThat(response.getMessage()).isEqualTo("Asset assigned successfully");
        assertThat(availableAsset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(allocationRepository).save(any(Allocation.class));
    }

    @Test
    void assignAsset_alreadyAssigned_throwsConflict() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(10L); req.setUserId(2L);

        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(allocationRepository.existsByAssetIdAndActiveTrue(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.assignAsset(req, 1L))
                .isInstanceOf(AssetAlreadyAssignedException.class);
    }

    @Test
    void assignAsset_expiredAsset_throwsBadRequest() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(12L); req.setUserId(2L);

        when(assetRepository.findById(12L)).thenReturn(Optional.of(expiredAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignAsset(req, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Expired");
    }

    @Test
    void assignAsset_assetNotFound_throwsNotFound() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(999L); req.setUserId(2L);

        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(req, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assignAsset_userNotFound_throwsNotFound() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(10L); req.setUserId(999L);

        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(req, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── 2. Return Asset ───────────────────────────────────────────────────────

    @Test
    void returnAsset_success() {
        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        MessageResponse response = service.returnAsset(100L);

        assertThat(response.getMessage()).isEqualTo("Asset returned successfully");
        assertThat(activeAllocation.isActive()).isFalse();
        assertThat(activeAllocation.getReturnedDate()).isNotNull();
        assertThat(assignedAsset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void returnAsset_alreadyReturned_throwsBadRequest() {
        activeAllocation.setActive(false);
        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.returnAsset(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already closed");
    }

    // ── 3. Transfer Asset ────────────────────────────────────────────────────

    @Test
    void transferAsset_success() {
        User newUser = User.builder().id(3L).name("NewDev").build();
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L); req.setNewUserId(3L); req.setNotes("transfer");

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        MessageResponse response = service.transferAsset(req, 1L);

        assertThat(response.getMessage()).isEqualTo("Asset transferred successfully");
        assertThat(activeAllocation.isActive()).isFalse();
        verify(allocationRepository, times(2)).save(any(Allocation.class));
    }

    @Test
    void transferAsset_sameUser_throwsBadRequest() {
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L);
        req.setNewUserId(developer.getId()); // same as current holder

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.transferAsset(req, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("current holder");
    }

    @Test
    void transferAsset_inactiveAllocation_throwsBadRequest() {
        activeAllocation.setActive(false);
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L); req.setNewUserId(3L);

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.transferAsset(req, 1L))
                .isInstanceOf(BadRequestException.class);
    }

    // ── 4. Allocation History ─────────────────────────────────────────────────

    @Test
    void getAllocationHistory_returnsHistory() {
        when(assetRepository.existsById(10L)).thenReturn(true);
        when(allocationRepository.findAllByAssetIdOrderByAssignedDateDesc(10L))
                .thenReturn(List.of(activeAllocation));
        when(mapper.toHistoryResponse(activeAllocation))
                .thenReturn(AllocationHistoryResponse.builder().user("Dev").active(true).build());

        List<AllocationHistoryResponse> result = service.getAllocationHistory(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser()).isEqualTo("Dev");
    }

    @Test
    void getAllocationHistory_assetNotFound_throwsNotFound() {
        when(assetRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.getAllocationHistory(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
