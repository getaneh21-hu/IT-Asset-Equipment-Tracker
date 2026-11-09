package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import com.It_Asset.it_asset_equipment_tracker.entity.Category;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.CategoryRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LowStockAlertService {

    private static final int ALERT_THRESHOLD = 2;

    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;

    public LowStockAlertService(
            AssetRepository assetRepository,
            CategoryRepository categoryRepository) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Map<String, Object>> getLowStockAlerts() {

        List<Asset> assets = assetRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> {

                    List<Asset> categoryAssets = assets.stream()
                            .filter(asset -> asset.getCategory() != null)
                            .filter(asset -> asset.getCategory().getId()
                                    .equals(category.getId()))
                            .toList();

                    long totalAssets = categoryAssets.size();

                    long availableAssets = categoryAssets.stream()
                            .filter(asset -> asset.getStatus()
                                    == AssetStatus.AVAILABLE)
                            .count();

                    long assignedAssets = categoryAssets.stream()
                            .filter(asset -> asset.getStatus()
                                    == AssetStatus.ASSIGNED)
                            .count();

                    long maintenanceAssets = categoryAssets.stream()
                            .filter(asset -> asset.getStatus()
                                    == AssetStatus.MAINTENANCE)
                            .count();

                    if (availableAssets > ALERT_THRESHOLD) {
                        return null;
                    }

                    return Map.<String, Object>of(
                            "categoryId", category.getId(),
                            "categoryName", category.getName(),
                            "totalAssets", totalAssets,
                            "availableAssets", availableAssets,
                            "assignedAssets", assignedAssets,
                            "maintenanceAssets", maintenanceAssets,
                            "alertThreshold", ALERT_THRESHOLD,
                            "alertStatus", "LOW_STOCK"
                    );
                })
                .filter(alert -> alert != null)
                .collect(Collectors.toList());
    }
}