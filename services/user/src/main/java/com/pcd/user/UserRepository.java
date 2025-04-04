package com.pcd.user;

import java.util.Optional;

import com.pcd.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);
    @Override
    Optional<User> findById(@NonNull String Id);
    void deleteById( @NonNull String id);
}