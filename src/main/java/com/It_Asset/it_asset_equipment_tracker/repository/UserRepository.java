
package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.It_Asset.it_asset_equipment_tracker.enums.Role;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    List<User> findByUsernameContainingIgnoreCase(String username);

long countByRoleAndEnabledTrue(Role role);

boolean existsByRoleAndEnabledTrue(Role role);
}
