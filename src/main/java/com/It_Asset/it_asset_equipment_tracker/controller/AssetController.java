
package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.dto.AssetAssignmentRequest;
import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.service.AssetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Create a new asset
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asset createAsset(@Valid @RequestBody Asset asset) {
        return assetService.createAsset(asset);
    }

    // Get all assets
    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    // Get one asset by ID
    @GetMapping("/{id}")
    public Asset getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }

    // Update an existing asset
    @PutMapping("/{id}")
    public Asset updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody Asset asset) {
        return assetService.updateAsset(id, asset);
    }

    // Assign an available asset to a user
    @PostMapping("/{id}/assign")
    public Asset assignAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetAssignmentRequest request) {
        return assetService.assignAsset(id, request.getUserId());
    }

    // Delete an asset
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
    }
}
