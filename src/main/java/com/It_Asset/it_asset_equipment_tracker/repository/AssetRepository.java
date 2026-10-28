
package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Check whether a serial number already exists.
    boolean existsBySerialNumber(String serialNumber);

    // Check whether another asset uses this serial number.
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    // Find assets by their status.
    List<Asset> findByStatus(AssetStatus status);

    // Find assets assigned to a user by username.
    List<Asset> findByAssignedToUsername(String username);
}
