package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrName(String email, String name);

    // Admin dashboard
    long countByActive(boolean active);
    long countByRole(String role);
    @Query("select u.createdAt from User u")
    List<Date> findAllCreatedAt();

}
