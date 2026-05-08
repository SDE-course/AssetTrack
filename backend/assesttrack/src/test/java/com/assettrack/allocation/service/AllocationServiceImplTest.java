package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AllocationHistoryResponse;
import com.assettrack.allocation.dto.AllocationResponse;
import com.assettrack.allocation.dto.AssignAssetRequest;
import com.assettrack.allocation.dto.MessageResponse;
import com.assettrack.allocation.dto.TransferAssetRequest;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.exception.AssetAlreadyAssignedException;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.mapper.AllocationMapper;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.AssetRepository;
import com.assettrack.allocation.repository.UserRepository;
import com.assettrack.usermanagement.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationServiceImplTest {

    @Mock private AllocationRepository allocationRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private UserRepository userRepository;
    @Mock private AllocationMapper mapper;

    @InjectMocks private AllocationServiceImpl service;

    private User admin;
    private User developer;
    private User frontendUser;
    private Asset availableAsset;
    private Asset assignedAsset;
    private Asset expiredAsset;
    private Allocation activeAllocation;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .fullName("Admin User")
                .email("admin@example.com")
                .password("secret")
                .role(Role.ADMIN)
                .active(true)
                .build();

        developer = User.builder()
                .id(2L)
                .fullName("Developer User")
                .email("dev@example.com")
                .password("secret")
                .role(Role.DEVELOPER)
                .active(true)
                .build();

        frontendUser = User.builder()
                .id(3L)
                .fullName("Frontend User")
                .email("frontend@example.com")
                .password("secret")
                .role(Role.DEVELOPER)
                .active(true)
                .build();

        availableAsset = Asset.builder()
                .id(10L)
                .name("MacBook Pro")
                .serialNumber("SN-001")
                .brand("Apple")
                .type("LAPTOP")
                .status(AssetStatus.AVAILABLE)
                .ram(16)
                .storage(512)
                .build();

        assignedAsset = Asset.builder()
                .id(11L)
                .name("Dell XPS")
                .serialNumber("SN-002")
                .brand("Dell")
                .type("LAPTOP")
                .status(AssetStatus.ASSIGNED)
                .ram(16)
                .storage(512)
                .build();

        expiredAsset = Asset.builder()
                .id(12L)
                .name("Old HP")
                .serialNumber("SN-003")
                .brand("HP")
                .type("LAPTOP")
                .status(AssetStatus.EXPIRED)
                .ram(8)
                .storage(256)
                .build();

        activeAllocation = Allocation.builder()
                .id(100L)
                .asset(assignedAsset)
                .assignedTo(developer)
                .assignedBy(admin)
                .assignedDate(LocalDateTime.now().minusDays(5))
                .active(true)
                .notes("Initial assignment")
                .build();
    }

    @Test
    void assignAsset_success() {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(availableAsset.getId());
        request.setUserId(developer.getId());
        request.setNotes("Assigned for backend development");

        when(assetRepository.findById(availableAsset.getId())).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(allocationRepository.existsByAssetIdAndActiveTrue(availableAsset.getId())).thenReturn(false);

        MessageResponse response = service.assignAsset(request, admin.getId());

        assertThat(response.getMessage()).isEqualTo("Asset assigned successfully");
        assertThat(availableAsset.getStatus()).isEqualTo(AssetStatus.ASSIGNED);
        verify(allocationRepository).save(any(Allocation.class));
        verify(assetRepository).save(availableAsset);
    }

    @Test
    void assignAsset_alreadyAssigned_throwsConflict() {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(availableAsset.getId());
        request.setUserId(developer.getId());

        when(assetRepository.findById(availableAsset.getId())).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(allocationRepository.existsByAssetIdAndActiveTrue(availableAsset.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.assignAsset(request, admin.getId()))
                .isInstanceOf(AssetAlreadyAssignedException.class)
                .hasMessageContaining("active allocation");

        verify(allocationRepository, never()).save(any());
    }

    @Test
    void assignAsset_expiredAsset_throwsBadRequest() {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(expiredAsset.getId());
        request.setUserId(developer.getId());

        when(assetRepository.findById(expiredAsset.getId())).thenReturn(Optional.of(expiredAsset));
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.assignAsset(request, admin.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Expired assets");
    }

    @Test
    void assignAsset_invalidAsset_throwsNotFound() {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(999L);
        request.setUserId(developer.getId());

        when(assetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(request, admin.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Asset not found");
    }

    @Test
    void assignAsset_invalidUser_throwsNotFound() {
        AssignAssetRequest request = new AssignAssetRequest();
        request.setAssetId(availableAsset.getId());
        request.setUserId(999L);

        when(assetRepository.findById(availableAsset.getId())).thenReturn(Optional.of(availableAsset));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignAsset(request, admin.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void returnAsset_success() {
        when(allocationRepository.findById(activeAllocation.getId())).thenReturn(Optional.of(activeAllocation));

        MessageResponse response = service.returnAsset(activeAllocation.getId());

        assertThat(response.getMessage()).isEqualTo("Asset returned successfully");
        assertThat(activeAllocation.isActive()).isFalse();
        assertThat(activeAllocation.getReturnedDate()).isNotNull();
        assertThat(activeAllocation.getAsset().getStatus()).isEqualTo(AssetStatus.AVAILABLE);
        verify(assetRepository).save(activeAllocation.getAsset());
    }

    @Test
    void returnAsset_alreadyReturned_throwsBadRequest() {
        activeAllocation.setActive(false);
        when(allocationRepository.findById(activeAllocation.getId())).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.returnAsset(activeAllocation.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already closed");
    }

    @Test
    void transferAsset_success() {
        TransferAssetRequest request = new TransferAssetRequest();
        request.setAllocationId(activeAllocation.getId());
        request.setNewUserId(frontendUser.getId());
        request.setNotes("Transferred to frontend developer");

        when(allocationRepository.findById(activeAllocation.getId())).thenReturn(Optional.of(activeAllocation));
        when(userRepository.findById(frontendUser.getId())).thenReturn(Optional.of(frontendUser));
        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        MessageResponse response = service.transferAsset(request, admin.getId());

        assertThat(response.getMessage()).isEqualTo("Asset transferred successfully");
        verify(allocationRepository, times(2)).save(any(Allocation.class));
        assertThat(activeAllocation.isActive()).isFalse();
    }

    @Test
    void transferAsset_inactiveAllocation_throwsBadRequest() {
        activeAllocation.setActive(false);
        TransferAssetRequest request = new TransferAssetRequest();
        request.setAllocationId(activeAllocation.getId());
        request.setNewUserId(frontendUser.getId());

        when(allocationRepository.findById(activeAllocation.getId())).thenReturn(Optional.of(activeAllocation));

        assertThatThrownBy(() -> service.transferAsset(request, admin.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void getAllocationHistory_returnsHistory() {
        AllocationHistoryResponse historyResponse = AllocationHistoryResponse.builder()
                .allocationId(activeAllocation.getId())
                .user("Developer User")
                .userId(developer.getId())
                .assignedBy("Admin User")
                .assignedDate(activeAllocation.getAssignedDate())
                .returnedDate(null)
                .active(true)
                .notes(activeAllocation.getNotes())
                .build();

        when(assetRepository.existsById(availableAsset.getId())).thenReturn(true);
        when(allocationRepository.findAllByAssetIdOrderByAssignedDateDesc(availableAsset.getId()))
                .thenReturn(List.of(activeAllocation));
        when(mapper.toHistoryResponse(activeAllocation)).thenReturn(historyResponse);

        List<AllocationHistoryResponse> result = service.getAllocationHistory(availableAsset.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser()).isEqualTo("Developer User");
    }

    @Test
    void getCurrentOwner_whenActiveAllocationExists_returnsMappedResponse() {
        when(assetRepository.existsById(assignedAsset.getId())).thenReturn(true);
        when(allocationRepository.findByAssetIdAndActiveTrue(assignedAsset.getId())).thenReturn(Optional.of(activeAllocation));
        when(mapper.toResponse(activeAllocation)).thenReturn(AllocationResponse.builder().id(activeAllocation.getId()).build());

        Object result = service.getCurrentOwner(assignedAsset.getId());

        assertThat(result).isInstanceOf(AllocationResponse.class);
        assertThat(((AllocationResponse) result).getId()).isEqualTo(activeAllocation.getId());
    }

    @Test
    void getCurrentOwner_whenNoActiveAllocation_returnsMessage() {
        when(assetRepository.existsById(availableAsset.getId())).thenReturn(true);
        when(allocationRepository.findByAssetIdAndActiveTrue(availableAsset.getId())).thenReturn(Optional.empty());

        Object result = service.getCurrentOwner(availableAsset.getId());

        assertThat(result).isInstanceOf(MessageResponse.class);
        assertThat(((MessageResponse) result).getMessage()).isEqualTo("Asset is currently available");
    }
}