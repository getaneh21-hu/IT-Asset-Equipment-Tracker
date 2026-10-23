
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
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AssetService(
            AssetRepository assetRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // Convert an Asset entity into a response DTO.
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

        return response;
    }

    // Convert a request DTO into an Asset entity.
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

    // Find and validate the selected category.
    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with ID: " + categoryId));
    }

    // Validate purchase and warranty dates.
    private void validateAssetDates(Asset asset) {
        LocalDate purchaseDate = asset.getPurchaseDate();
        LocalDate warrantyExpiryDate = asset.getWarrantyExpiryDate();

        if (purchaseDate != null
                && warrantyExpiryDate != null
                && warrantyExpiryDate.isBefore(purchaseDate)) {
            throw new BadRequestException(
                    "Warranty expiry date cannot be before purchase date");
        }
    }

    // Validate required asset fields.
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

    // Create a new asset.
    @Transactional
    public AssetResponse createAsset(AssetRequest request) {
        validateRequiredFields(request);

        Asset asset = toEntity(request);
        validateAssetDates(asset);

        // Assign the requested category, if provided.
        asset.setCategory(getCategory(request.getCategoryId()));

        if (assetRepository.existsBySerialNumber(asset.getSerialNumber())) {
            throw new BadRequestException(
                    "Serial number already exists: "
                            + asset.getSerialNumber());
        }

        asset.setStatus(
                request.getStatus() == null
                        ? AssetStatus.AVAILABLE
                        : request.getStatus());

        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Use the assign endpoint to assign an asset to a user");
        }

        asset.setAssignedTo(null);
        asset.setAssignedToName(null);

        return toResponse(assetRepository.save(asset));
    }

    // Get all assets.
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Get assets filtered by status.
    @Transactional(readOnly = true)
    public List<AssetResponse> getAssetsByStatus(AssetStatus status) {
        if (status == null) {
            return getAllAssets();
        }

        return assetRepository.findAll()
                .stream()
                .filter(asset -> asset.getStatus() == status)
                .map(this::toResponse)
                .toList();
    }

    // Get assets assigned to a particular user.
    @Transactional(readOnly = true)
    public List<AssetResponse> getMyAssets(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        return assetRepository.findAll()
                .stream()
                .filter(asset -> asset.getAssignedTo() != null
                        && asset.getAssignedTo().getId().equals(user.getId()))
                .map(this::toResponse)
                .toList();
    }

    // Get one asset by ID.
    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + id));

        return toResponse(asset);
    }

    // Update an existing asset without changing its assignment or status.
    @Transactional
    public AssetResponse updateAsset(Long id, AssetRequest request) {
        validateRequiredFields(request);

        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + id));

        Asset candidate = toEntity(request);
        validateAssetDates(candidate);

        if (!existingAsset.getSerialNumber().equals(request.getSerialNumber())
                && assetRepository.existsBySerialNumber(
                        request.getSerialNumber())) {
            throw new BadRequestException(
                    "Serial number already exists: "
                            + request.getSerialNumber());
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

        // Update category only when a category ID is supplied.
        if (request.getCategoryId() != null) {
            existingAsset.setCategory(getCategory(request.getCategoryId()));
        }

        return toResponse(assetRepository.save(existingAsset));
    }

    // Assign an available asset to an existing user.
    @Transactional
    public AssetResponse assignAsset(Long assetId, Long userId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Asset can only be assigned when status is AVAILABLE");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId));

        asset.setAssignedTo(user);
        asset.setAssignedToName(user.getUsername());
        asset.setStatus(AssetStatus.ASSIGNED);

        return toResponse(assetRepository.save(asset));
    }

    // Return an assigned asset.
    @Transactional
    public AssetResponse returnAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Only an assigned asset can be returned");
        }

        asset.setAssignedTo(null);
        asset.setAssignedToName(null);
        asset.setStatus(AssetStatus.AVAILABLE);

        return toResponse(assetRepository.save(asset));
    }

    // Update an asset's status.
    @Transactional
    public AssetResponse updateAssetStatus(
            Long assetId, AssetStatus newStatus) {

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + assetId));

        if (newStatus == null) {
            throw new BadRequestException("Asset status is required");
        }

        if (newStatus == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "Use the assign endpoint to assign an asset");
        }

        if (asset.getStatus() == AssetStatus.ASSIGNED) {
            throw new BadRequestException(
                    "An assigned asset cannot change status. Unassign it first.");
        }

        asset.setStatus(newStatus);
        asset.setAssignedTo(null);
        asset.setAssignedToName(null);

        return toResponse(assetRepository.save(asset));
    }

    // Delete an asset.
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + id));

        assetRepository.delete(asset);
    }
}
