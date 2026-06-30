package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

}
