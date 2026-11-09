package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryRepository
        extends JpaRepository<AssetHistory, Long> {

    List<AssetHistory> findByAssetIdOrderByCreatedAtDesc(Long assetId);
}