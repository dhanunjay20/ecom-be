package com.tcon.ecom.repository;

import com.tcon.ecom.model.User;
import com.tcon.ecom.model.enums.UserRole;
import com.tcon.ecom.model.enums.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByGithubId(String githubId);
    Optional<User> findByFacebookId(String facebookId);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    Optional<User> findByRefreshToken(String refreshToken);
    Boolean existsByEmail(String email);
    Long countByRole(UserRole role);
    Long countByStatus(UserStatus status);
}

