package com.It_Asset.it_asset_equipment_tracker.controller;

import com.It_Asset.it_asset_equipment_tracker.dto.UserResponse;
import com.It_Asset.it_asset_equipment_tracker.dto.UpdateUserRoleRequest;
import com.It_Asset.it_asset_equipment_tracker.dto.UpdateUserStatusRequest;
import com.It_Asset.it_asset_equipment_tracker.service.UserManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(
            UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // List all users or search by username.
    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestParam(required = false) String search) {
        return userManagementService.getAllUsers(search);
    }

    // Get one user's details.
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userManagementService.getUserById(id);
    }

    // Change a user's role.
    @PatchMapping("/{id}/role")
    public UserResponse updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {

        return userManagementService.updateUserRole(
                id, request, authentication.getName());
    }

    // Activate or deactivate a user.
    @PatchMapping("/{id}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {

        return userManagementService.updateUserStatus(
                id, request.getEnabled(), authentication.getName());
    }

    // Delete a user.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        userManagementService.deleteUser(
                id, authentication.getName());
    }
}