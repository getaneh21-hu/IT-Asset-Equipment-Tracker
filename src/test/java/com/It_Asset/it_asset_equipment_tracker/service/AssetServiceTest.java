
package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.dto.AssetRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetResponse;
import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.It_Asset.it_asset_equipment_tracker.exception.BadRequestException;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.CategoryRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AssetService assetService;

    private AssetRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AssetRequest();
        validRequest.setName("Test Laptop");
        validRequest.setType("Laptop");
        validRequest.setSerialNumber("TEST-SERIAL-001");
        validRequest.setPurchaseCost(new BigDecimal("50000.00"));
        validRequest.setPurchaseDate(LocalDate.of(2026, 1, 1));
        validRequest.setStatus(null);
    }

    @Test
    void createAsset_validRequest_savesAssetAsAvailable() {
        when(assetRepository.existsBySerialNumber("TEST-SERIAL-001"))
                .thenReturn(false);

        when(assetRepository.save(any(Asset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AssetResponse response = assetService.createAsset(validRequest);

        assertNotNull(response);
        assertEquals("Test Laptop", response.getName());
        assertEquals(AssetStatus.AVAILABLE, response.getStatus());

        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void createAsset_duplicateSerialNumber_throwsBadRequestException() {
        when(assetRepository.existsBySerialNumber("TEST-SERIAL-001"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> assetService.createAsset(validRequest)
        );

        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void createAsset_warrantyBeforePurchase_throwsBadRequestException() {
        validRequest.setWarrantyExpiryDate(LocalDate.of(2025, 12, 31));

        assertThrows(
                BadRequestException.class,
                () -> assetService.createAsset(validRequest)
        );

        verify(assetRepository, never()).save(any(Asset.class));
    }
}
