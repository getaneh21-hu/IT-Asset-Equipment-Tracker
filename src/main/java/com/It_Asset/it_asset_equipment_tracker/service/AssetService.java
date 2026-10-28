
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

    public AssetService(
            AssetRepository assetRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
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

    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId));
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
        Asset savedAsset = assetRepository.save(existingAsset);

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

        asset.setStatus(newStatus);
        asset.setAssignedTo(null);
        asset.setAssignedToName(null);

        Asset savedAsset = assetRepository.save(asset);

        return toResponse(savedAsset);
    }

    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + id));

        assetRepository.delete(asset);
    }
}
