package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetHistory;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetHistoryRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetHistoryService {

    private final AssetHistoryRepository historyRepository;

    public AssetHistoryService(AssetHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void record(
            Asset asset,
            String action,
            String description) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            username = authentication.getName();
        }

        AssetHistory history = new AssetHistory();

        history.setAssetId(asset.getId());
        history.setAssetName(asset.getName());
        history.setSerialNumber(asset.getSerialNumber());
        history.setAction(action);
        history.setDescription(description);
        history.setUsername(username);

        historyRepository.save(history);
    }

    public List<AssetHistory> getAssetHistory(Long assetId) {
        return historyRepository
                .findByAssetIdOrderByCreatedAtDesc(assetId);
    }
}