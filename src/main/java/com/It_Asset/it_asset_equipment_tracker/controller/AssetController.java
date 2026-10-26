
package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.dto.AssetRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetResponse;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetStatusUpdateRequest;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.It_Asset.it_asset_equipment_tracker.service.AssetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // Create an asset.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse createAsset(
            @Valid @RequestBody AssetRequest request) {
        return assetService.createAsset(request);
    }

    // List all assets, optionally filtered by status.
    @GetMapping
    public List<AssetResponse> getAllAssets(
            @RequestParam(required = false) AssetStatus status) {
        return assetService.getAssetsByStatus(status);
    }

    // List assets assigned to the authenticated user.
    @GetMapping("/my-assets")
    public List<AssetResponse> getMyAssets(Authentication authentication) {
        return assetService.getMyAssets(authentication.getName());
    }

    // Get one asset.
    @GetMapping("/{id}")
    public AssetResponse getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }

    // Update asset details.
    @PutMapping("/{id}")
    public AssetResponse updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        return assetService.updateAsset(id, request);
    }

    // Assign an asset to a user.
    @PostMapping("/{id}/assign/{userId}")
    public AssetResponse assignAsset(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return assetService.assignAsset(id, userId);
    }

    // Return an assigned asset.
    @GetMapping("/{id}/return")
    public AssetResponse returnAsset(@PathVariable Long id) {
        return assetService.returnAsset(id);
    }

    // Update asset status.
    @PatchMapping("/{id}/status")
    public AssetResponse updateAssetStatus(
            @PathVariable Long id,
            @Valid @RequestBody AssetStatusUpdateRequest request) {
        return assetService.updateAssetStatus(id, request.getStatus());
    }

    // Delete an asset.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
    }
}
