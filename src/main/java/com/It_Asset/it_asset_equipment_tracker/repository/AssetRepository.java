
package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.Asset;
import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AssetRepository extends JpaRepository<Asset, Long> {

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, long id);

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByAssignedToUsername(String username);
}
