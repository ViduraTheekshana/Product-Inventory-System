package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // Excludes DELETED - a deleted user is invisible to normal listing,
    // exactly matching how Product already treats deleted rows.
    Page<User> findAllByStatusNot(UserStatus excludedStatus, Pageable pageable);

    // Excludes DELETED - any lookup used before modifying a user (suspend,
    // reactivate, role change) will correctly fail to find a deleted one,
    // making every mutation on a deleted user a clean 404, not a silent
    // "undelete."
    Optional<User> findByIdAndStatusNot(UUID id, UserStatus excludedStatus);
}