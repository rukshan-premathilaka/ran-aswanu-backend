package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByName(String name);
    boolean existsByEmail(String email);

}
