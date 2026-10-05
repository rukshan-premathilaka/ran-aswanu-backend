package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrName(String email, String name);

    // ---- admin: counts ----
    long countByActive(boolean active);

    @Query("select count(distinct u) from User u join u.roles r where r.name = :role")
    long countByRole(@Param("role") String role);

    @Query("select count(u) from User u where u.roles is empty")
    long countWithoutRoles();

    // from is included, to is NOT included
    @Query("select count(u) from User u where u.createdAt >= :from and u.createdAt < :to")
    long countCreatedBetween(@Param("from") Date from, @Param("to") Date to);

    @Query("select min(u.createdAt) from User u")
    Date findEarliestCreatedAt();
}
