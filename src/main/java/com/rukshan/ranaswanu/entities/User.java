package com.rukshan.ranaswanu.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "is_active")
    private boolean active;


    @Column(name = "role")
    private String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            schema = "dbo",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new LinkedHashSet<>();

    @Column(name = "profile_picture")
    private String ProfilePicture;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    public boolean hasRole(String roleName) {
        if (roleName == null) return false;
        String wanted = roleName.trim().toUpperCase(Locale.ROOT);
        return roles != null && roles.stream()
                .filter(r -> r != null && r.getName() != null)
                .anyMatch(r -> wanted.equals(r.getName().trim().toUpperCase(Locale.ROOT)));
    }

    public void addRole(Role roleEntity) {
        if (roleEntity != null) {
            if (roles == null) roles = new LinkedHashSet<>();
            roles.add(roleEntity);
        }
    }

    public void removeRole(String roleName) {
        if (roles == null || roleName == null) return;
        String wanted = roleName.trim().toUpperCase(Locale.ROOT);
        roles.removeIf(r -> r != null && r.getName() != null
                && wanted.equals(r.getName().trim().toUpperCase(Locale.ROOT)));
    }
}
