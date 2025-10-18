package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(String provider, String oauthId);
}

