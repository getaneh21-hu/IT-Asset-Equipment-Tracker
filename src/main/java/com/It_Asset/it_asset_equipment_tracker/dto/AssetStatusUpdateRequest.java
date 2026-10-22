
package com.It_Asset.it_asset_equipment_tracker.dto;

import com.It_Asset.it_asset_equipment_tracker.entity.AssetStatus;
import jakarta.validation.constraints.NotNull;

public class AssetStatusUpdateRequest {

    @NotNull(message = "Asset status is required")
    private AssetStatus status;

    public AssetStatus getStatus() {
        return status;
    }

    public void setStatus(AssetStatus status) {
        this.status = status;
    }
}
