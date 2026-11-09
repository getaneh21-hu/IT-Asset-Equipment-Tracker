package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.dto.UserResponse;
import com.It_Asset.it_asset_equipment_tracker.dto.UpdateUserRoleRequest;
import com.It_Asset.it_asset_equipment_tracker.entity.User;
import com.It_Asset.it_asset_equipment_tracker.enums.Role;
import com.It_Asset.it_asset_equipment_tracker.repository.UserRepository;
import com.It_Asset.it_asset_equipment_tracker.repository.AssetRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public UserManagementService(
            UserRepository userRepository,
            AssetRepository assetRepository) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String search) {

        List<User> users;

        if (search == null || search.trim().isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByUsernameContainingIgnoreCase(
                    search.trim());
        }

        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUser(id);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(
            Long id,
            UpdateUserRoleRequest request,
            String adminUsername) {

        User user = findUser(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot change your own role");
        }

        if (user.getRole() == Role.ROLE_ADMIN
                && request.getRole() != Role.ROLE_ADMIN
                && userRepository.countByRoleAndEnabledTrue(
                        Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot remove the last active administrator");
        }

        user.setRole(request.getRole());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUserStatus(
            Long id,
            boolean enabled,
            String adminUsername) {

        User user = findUser(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot deactivate your own account");
        }

        if (!enabled
                && user.isEnabled()
                && user.getRole() == Role.ROLE_ADMIN
                && userRepository.countByRoleAndEnabledTrue(
                        Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot deactivate the last active administrator");
        }

        user.setEnabled(enabled);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id, String adminUsername) {

        User user = findUser(id);

        if (user.getUsername().equals(adminUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete your own account");
        }

        if (user.isEnabled()
                && user.getRole() == Role.ROLE_ADMIN
                && userRepository.countByRoleAndEnabledTrue(
                        Role.ROLE_ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete the last active administrator");
        }

        if (!assetRepository.findByAssignedToUsername(
                user.getUsername()).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This user still has assigned assets. Return or reassign them first.");
        }

        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled());
    }
}