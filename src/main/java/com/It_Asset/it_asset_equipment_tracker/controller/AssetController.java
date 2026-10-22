
package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.dto.AssetAssignmentRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetResponse;
import com.It_Asset.it_asset_equipment_tracker.dto.AssetStatusUpdateRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse createAsset(
            @Valid @RequestBody AssetRequest request) {
        return assetService.createAsset(request);
    }

    @GetMapping
    public List<AssetResponse> getAllAssets() {
        return assetService.getAllAssets();
    }

    @GetMapping("/{id}")
    public AssetResponse getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }

    @PutMapping("/{id}")
    public AssetResponse updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest request) {
        return assetService.updateAsset(id, request);
    }

    @PostMapping("/{id}/assign")
    public AssetResponse assignAsset(
            @PathVariable Long id,
            @Valid @RequestBody AssetAssignmentRequest request) {
        return assetService.assignAsset(id, request.getUserId());
    }

    @PatchMapping("/{id}/status")
    public AssetResponse updateAssetStatus(
            @PathVariable Long id,
            @Valid @RequestBody AssetStatusUpdateRequest request) {
        return assetService.updateAssetStatus(
                id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
    }
}
