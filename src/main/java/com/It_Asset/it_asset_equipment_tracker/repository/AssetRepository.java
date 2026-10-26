
package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Find an asset by its serial number.
    Optional<Asset> findBySerialNumber(String serialNumber);

    // Check whether a serial number already exists.
    boolean existsBySerialNumber(String serialNumber);

    // Find all assets with the specified status.
    List<Asset> findByStatus(AssetStatus status);
}
