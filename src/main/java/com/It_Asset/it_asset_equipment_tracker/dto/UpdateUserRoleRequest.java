package com.It_Asset.it_asset_equipment_tracker.dto;

import com.It_Asset.it_asset_equipment_tracker.enums.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}