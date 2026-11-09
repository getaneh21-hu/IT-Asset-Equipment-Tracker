
package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.entity.AssetHistory;
import com.It_Asset.it_asset_equipment_tracker.service.AssetHistoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-history")
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    public AssetHistoryController(
            AssetHistoryService assetHistoryService) {
        this.assetHistoryService = assetHistoryService;
    }

    // Get history for a specific asset
    @GetMapping("/{assetId}")
    public ResponseEntity<List<AssetHistory>> getAssetHistory(
            @PathVariable Long assetId) {

        List<AssetHistory> history =
                assetHistoryService.getAssetHistory(assetId);

        return ResponseEntity.ok(history);
    }
}
