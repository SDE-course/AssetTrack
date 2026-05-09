package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssignAssetRequest;
import com.assettrack.allocation.dto.MessageResponse;
import com.assettrack.allocation.dto.TransferAssetRequest;
import com.assettrack.allocation.entity.*;
import com.assettrack.allocation.exception.*;
import com.assettrack.allocation.mapper.AllocationMapper;
import com.assettrack.allocation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {

    @Mock AllocationRepository allocationRepository;
    @Mock AssetRepository      assetRepository;
    @Mock UserRepository       userRepository;
    @Mock AllocationMapper     mapper;

    @InjectMocks
    AllocationServiceImpl service;

    // ── Fixtures ──────────────────────────────────────────────────────────────
    private User admin;
    private User developer;
    private Asset availableAsset;
    private Asset assignedAsset;
    private Asset expiredAsset;
    private Allocation activeAllocation;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).name("Admin").email("admin@test.com")
                .role(Role.ADMIN).build();

        developer = User.builder().id(2L).name("Dev").email("dev@test.com")
                .role(Role.DEVELOPER).build();

        availableAsset = Asset.builder().id(10L).name("Dell XPS")
                .serialNumber("SN-001").status(AssetStatus.AVAILABLE).build();

        assignedAsset = Asset.builder().id(11L).name("MacBook")
                .serialNumber("SN-002").status(AssetStatus.ASSIGNED).build();

        expiredAsset = Asset.builder().id(12L).name("Old Laptop")
                .serialNumber("SN-OLD").status(AssetStatus.EXPIRED).build();

        activeAllocation = Allocation.builder()
                .id(100L)
                .asset(assignedAsset)
                .assignedTo(developer)
                .assignedBy(admin)
                .assignedDate(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASSIGN TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Assign available asset → success")
    void assignAsset_available_success() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(10L);
        req.setUserId(2L);
        req.setNotes("Test note");

        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(allocationRepository.existsByAssetIdAndActiveTrue(10L)).thenReturn(false);

        MessageResponse response = service.assignAsset(req, "admin@test.com");

        assertThat(response.getMessage()).isEqualTo("Asset assigned successfully");
        verify(allocationRepository).save(any(Allocation.class));
        assertThat(availableAsset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Assign already-assigned asset → conflict exception")
    void assignAsset_alreadyAssigned_throwsException() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(11L);
        req.setUserId(2L);

        when(assetRepository.findById(11L)).thenReturn(Optional.of(assignedAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignAsset(req, "admin@test.com"))
                .isInstanceOf(AssetAlreadyAssignedException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Assign expired asset → bad request exception")
    void assignAsset_expiredAsset_throwsException() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(12L);
        req.setUserId(2L);

        when(assetRepository.findById(12L)).thenReturn(Optional.of(expiredAsset));
        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignAsset(req, "admin@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Assign with invalid asset id → not found exception")
    void assignAsset_invalidAssetId_throwsException() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(999L);
        req.setUserId(2L);

        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(req, "admin@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asset not found");
    }

    @Test
    @DisplayName("Assign with invalid user id → not found exception")
    void assignAsset_invalidUserId_throwsException() {
        AssignAssetRequest req = new AssignAssetRequest();
        req.setAssetId(10L);
        req.setUserId(999L);

        when(assetRepository.findById(10L)).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(req, "admin@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RETURN TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Return active allocation → success")
    void returnAsset_active_success() {
        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        MessageResponse response = service.returnAsset(100L);

        assertThat(response.getMessage()).isEqualTo("Asset returned successfully");
        assertThat(activeAllocation.isActive()).isFalse();
        assertThat(activeAllocation.getReturnedDate()).isNotNull();
        assertThat(assignedAsset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Return already-returned allocation → bad request")
    void returnAsset_alreadyReturned_throwsException() {
        activeAllocation.setActive(false);
        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.returnAsset(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already closed");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFER TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Transfer asset to new user → success + history preserved")
    void transferAsset_success() {
        User newUser = User.builder().id(3L).name("New Dev").email("new@test.com")
                .role(Role.DEVELOPER).build();

        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L);
        req.setNewUserId(3L);
        req.setNotes("Transferred to frontend team");

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newUser));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        MessageResponse response = service.transferAsset(req, "admin@test.com");

        assertThat(response.getMessage()).isEqualTo("Asset transferred successfully");

        // Old allocation must be closed
        assertThat(activeAllocation.isActive()).isFalse();
        assertThat(activeAllocation.getReturnedDate()).isNotNull();

        // A NEW allocation should have been saved
        verify(allocationRepository, times(2)).save(any(Allocation.class));

        // Asset must stay ASSIGNED (not freed)
        assertThat(assignedAsset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
    }

    @Test
    @DisplayName("Transfer to same user → bad request")
    void transferAsset_sameUser_throwsException() {
        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L);
        req.setNewUserId(developer.getId()); // same as current holder

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.transferAsset(req, "admin@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("same user");
    }

    @Test
    @DisplayName("Transfer inactive allocation → bad request")
    void transferAsset_inactiveAllocation_throwsException() {
        activeAllocation.setActive(false);

        TransferAssetRequest req = new TransferAssetRequest();
        req.setAllocationId(100L);
        req.setNewUserId(3L);

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.transferAsset(req, "admin@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("inactive");
    }
}
