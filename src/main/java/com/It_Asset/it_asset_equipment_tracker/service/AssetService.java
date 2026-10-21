
package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.It_Asset.it_asset_equipment_tracker.entity.User;
import com.It_Asset.it_asset_equipment_tracker.exception.BadRequestException;
import com.It_Asset.it_asset_equipment_tracker.exception.ResourceNotFoundException;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetService(
            AssetRepository assetRepository,
            UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    // Create a new asset
    @Transactional
    public Asset createAsset(Asset asset) {

        if (asset.getName() == null || asset.getName().isBlank()) {
            throw new BadRequestException("Asset name is required");
        }

        if (asset.getSerialNumber() == null
                || asset.getSerialNumber().isBlank()) {
            throw new BadRequestException("Serial number is required");
        }

        if (assetRepository.existsBySerialNumber(
                asset.getSerialNumber())) {
            throw new BadRequestException(
                    "Serial number already exists: "
                            + asset.getSerialNumber());
        }

        // New assets must start as AVAILABLE.
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setAssignedTo(null);

        return assetRepository.save(asset);
    }

    // Get all assets
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    // Get one asset by ID
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with ID: " + id));
    }

    // Update an existing asset
    @Transactional
    public Asset updateAsset(Long id, Asset updatedAsset) {

        Asset existingAsset = getAssetById(id);

        if (updatedAsset.getName() == null
                || updatedAsset.getName().isBlank()) {
            throw new BadRequestException("Asset name is required");
        }

        if (updatedAsset.getSerialNumber() == null
                || updatedAsset.getSerialNumber().isBlank()) {
            throw new BadRequestException("Serial number is required");
        }

        if (!existingAsset.getSerialNumber()
                .equals(updatedAsset.getSerialNumber())
                && assetRepository.existsBySerialNumber(
                        updatedAsset.getSerialNumber())) {
            throw new BadRequestException(
                    "Serial number already exists: "
                            + updatedAsset.getSerialNumber());
        }

        existingAsset.setName(updatedAsset.getName());
        existingAsset.setType(updatedAsset.getType());
        existingAsset.setBrand(updatedAsset.getBrand());
        existingAsset.setModel(updatedAsset.getModel());
        existingAsset.setSerialNumber(updatedAsset.getSerialNumber());
        existingAsset.setDepartment(updatedAsset.getDepartment());
        existingAsset.setPurchaseCost(updatedAsset.getPurchaseCost());
        existingAsset.setPurchaseDate(updatedAsset.getPurchaseDate());
        existingAsset.setWarrantyExpiryDate(
                updatedAsset.getWarrantyExpiryDate());

        // Assignment and status are managed separately.
        return assetRepository.save(existingAsset);
    }

    // Assign an available asset to an existing user
    @Transactional
    public Asset assignAsset(Long assetId, Long userId) {

        Asset asset = getAssetById(assetId);

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

        return assetRepository.save(asset);
    }

    // Delete an asset
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        assetRepository.delete(asset);
    }
}
