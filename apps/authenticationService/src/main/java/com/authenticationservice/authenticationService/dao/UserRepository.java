package com.authenticationservice.authenticationService.dao;

import com.authenticationservice.authenticationService.models.CustomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<CustomUser, UUID> {
    Optional<CustomUser> findByEmail(String email);

}
