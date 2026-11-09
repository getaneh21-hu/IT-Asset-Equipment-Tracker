
package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.dto.AssetRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetResponse;
import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.It_Asset.it_asset_equipment_tracker.entity.Category;
import com.It_Asset.it_asset_equipment_tracker.entity.User;
import com.It_Asset.it_asset_equipment_tracker.exception.BadRequestException;
import com.It_Asset.it_asset_equipment_tracker.exception.ResourceNotFoundException;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.CategoryRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AssetHistoryService assetHistoryService;
    private final NotificationService notificationService;

    public AssetService(
            AssetRepository assetRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AssetHistoryService assetHistoryService,
            NotificationService notificationService)

            {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.assetHistoryService = assetHistoryService;
        this.notificationService = notificationService;
    }

    private AssetResponse toResponse(Asset asset) {
        AssetResponse response = new AssetResponse();

        response.setId(asset.getId());
        response.setName(asset.getName());
        response.setType(asset.getType());
        response.setBrand(asset.getBrand());
        response.setModel(asset.getModel());
        response.setSerialNumber(asset.getSerialNumber());
        response.setAssignedToName(asset.getAssignedToName());
        if (asset.getAssignedTo() != null) {
    response.setAssignedUserId(asset.getAssignedTo().getId());
}
        response.setDepartment(asset.getDepartment());

        if (asset.getCategory() != null) {
            response.setCategoryId(asset.getCategory().getId());
            response.setCategoryName(asset.getCategory().getName());
        }

        response.setStatus(asset.getStatus());
        response.setPurchaseCost(asset.getPurchaseCost());
        response.setPurchaseDate(asset.getPurchaseDate());
        response.setWarrantyExpiryDate(asset.getWarrantyExpiryDate());

        // Include the automatically generated creation timestamp.
        response.setCreatedAt(asset.getCreatedAt());

        return response;
    }

    private Asset toEntity(AssetRequest request) {
        Asset asset = new Asset();

        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setDepartment(request.getDepartment());
        asset.setPurchaseCost(request.getPurchaseCost());
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());

        return asset;
    }
    //

 private Category getCategory(Long categoryId) {
    if (categoryId == null) {
        return null;
 }

      Long requiredCategoryId = java.util.Objects.requireNonNull(categoryId);

        return categoryRepository.findById(requiredCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + requiredCategoryId));
}
    private void validateAssetDates(AssetRequest request) {
        LocalDate purchaseDate = request.getPurchaseDate();
        LocalDate warrantyExpiryDate = request.getWarrantyExpiryDate();


        if (purchaseDate != null
                && warrantyExpiryDate != null
                && warrantyExpiryDate.isBefore(purchaseDate)) {
            throw new BadRequestException(
                    "Warranty expiry date cannot be before purchase date");
        }
    }

    private void validateRequiredFields(AssetRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Asset name is required");
        }

        if (request.getType() == null || request.getType().isBlank()) {
            throw new BadRequestException("Asset type is required");
        }

        if (request.getSerialNumber() == null
                || request.getSerialNumber().isBlank()) {
            throw new BadRequestException("Serial number is required");
        }
    }

    public AssetResponse createAsset(AssetRequest request) {
        validateRequiredFields(request);
        validateAssetDates(request);

        if (assetRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BadRequestException(
                    "Serial number already exists: " + request.getSerialNumber());
        }

        Asset asset = toEntity(request);
        asset.setCategory(getCategory(request.getCategoryId()));

        if (request.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "An asset cannot be created with ASSIGNED status");
        }

        asset.setStatus(
                request.getStatus() == null
                        ? AssetStatus.AVAILABLE
                        : request.getStatus());

        asset.setAssignedTo(null);
        asset.setAssignedToName(null);
        
        Asset savedAsset = assetRepository.save(asset);

// Record asset creation history
assetHistoryService.record(
        savedAsset,
        "CREATED",
        "Asset was created and added to inventory."
);

// Create notification
notificationService.createNotification(
        "New Asset Created",
        "A new " + savedAsset.getName()
                + " asset has been added to inventory. Serial number: "
                + savedAsset.getSerialNumber(),
        "ASSET_CREATED"
);

return toResponse(savedAsset);
    }
        
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssetsByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getMyAssets(String username) {
        return assetRepository.findByAssignedToUsername(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + id));

        return toResponse(asset);
    }

    public AssetResponse updateAsset(Long id, AssetRequest request) {
        validateRequiredFields(request);
        validateAssetDates(request);

        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + id));

        boolean serialNumberUsed =
                assetRepository.existsBySerialNumberAndIdNot(
                        request.getSerialNumber(), id);

        if (serialNumberUsed) {
            throw new BadRequestException(
                    "Serial number already exists: " + request.getSerialNumber());
        }

        existingAsset.setName(request.getName());
        existingAsset.setType(request.getType());
        existingAsset.setBrand(request.getBrand());
        existingAsset.setModel(request.getModel());
        existingAsset.setSerialNumber(request.getSerialNumber());
        existingAsset.setDepartment(request.getDepartment());
        existingAsset.setPurchaseCost(request.getPurchaseCost());
        existingAsset.setPurchaseDate(request.getPurchaseDate());
        existingAsset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());

        if (request.getCategoryId() != null) {
            existingAsset.setCategory(getCategory(request.getCategoryId()));
        }

        // Preserve the current assignment and status during an ordinary update.

        // Preserve the current assignment and status during an ordinary update.
        Asset savedAsset = assetRepository.save(existingAsset);

        assetHistoryService.record(
        savedAsset,
        "UPDATED",
        "Asset details were updated."
        );

        return toResponse(savedAsset);
                }

    public AssetResponse assignAsset(Long assetId, Long userId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + assetId));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Only AVAILABLE assets can be assigned");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        asset.setAssignedTo(user);
        asset.setAssignedToName(user.getUsername());
        asset.setStatus(AssetStatus.ASSIGNED);

        Asset savedAsset = assetRepository.save(asset);
        notificationService.createNotification(
        "Asset Assigned",
        "Asset '" + savedAsset.getName()
                + "' was assigned to " + user.getUsername() + ".",
        "ASSET_ASSIGNED"
);

        assetHistoryService.record(
        savedAsset,
        "ASSIGNED",
        "Asset was assigned to " + user.getUsername() + "."
    );

return toResponse(savedAsset);
    }

    public AssetResponse returnAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + assetId));

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Only ASSIGNED assets can be returned");
        }

        asset.setAssignedTo(null);
        asset.setAssignedToName(null);
        asset.setStatus(AssetStatus.AVAILABLE);

        Asset savedAsset = assetRepository.save(asset);

        assetHistoryService.record(
                savedAsset,
                "RETURNED",
                "Asset was returned."
        );

        return toResponse(savedAsset);
    }

    public AssetResponse updateAssetStatus(
            Long assetId, AssetStatus newStatus) {

        if (newStatus == null) {
            throw new BadRequestException("Asset status is required");
        }

        if (newStatus == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Use the assign endpoint to assign an asset");
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + assetId));

        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Return the asset before changing its status");
        }
        AssetStatus oldStatus = asset.getStatus();

        asset.setStatus(newStatus);
        asset.setAssignedTo(null);
        asset.setAssignedToName(null);


        Asset savedAsset = assetRepository.save(asset);

// Record asset history
assetHistoryService.record(
        savedAsset,
        "STATUS_UPDATED",
        "Asset status was updated from " + oldStatus
                + " to " + newStatus + "."
);

// Create notification
notificationService.createNotification(
        "Asset Status Updated",
        "Asset '" + savedAsset.getName()
                + "' status changed from " + oldStatus
                + " to " + newStatus + ".",
        "ASSET_STATUS_UPDATED"
);

return toResponse(savedAsset);


    }
    // Delete asset and record history
    public void deleteAsset(Long id) {
    Asset asset = assetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Asset not found with id: " + id));

    // Record asset history
    assetHistoryService.record(
            asset,
            "DELETED",
            "Asset was deleted from the inventory."
    );

    // Create notification
    notificationService.createNotification(
            "Asset Deleted",
            "Asset '" + asset.getName()
                    + "' was deleted from the inventory. Serial number: "
                    + asset.getSerialNumber(),
            "ASSET_DELETED"
    );

    // Delete asset
    assetRepository.delete(asset);
}
}
