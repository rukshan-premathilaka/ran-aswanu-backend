package com.rukshan.ranaswanu.repository;

import com.rukshan.ranaswanu.entities.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>, JpaSpecificationExecutor<User> {

    // PostgreSQL compares text case-sensitively (SQL Server did not), so these are done with lower()
    // to keep login / duplicate checks working the same way as before.
    @Query("select count(u) > 0 from User u where lower(u.name) = lower(:name)")
    boolean existsByName(@Param("name") String name);

    @Query("select count(u) > 0 from User u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select u from User u where lower(u.email) = lower(:email) or lower(u.name) = lower(:name)")
    Optional<User> findByEmailOrName(@Param("email") String email, @Param("name") String name);

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
