package com.rukshan.ranaswanu.config;

import com.rukshan.ranaswanu.entities.Role;
import com.rukshan.ranaswanu.entities.User;
import com.rukshan.ranaswanu.repository.RoleRepository;
import com.rukshan.ranaswanu.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * Creates up to 5 admin accounts when the server starts (admins can NOT be created from the frontend).
 * Values come from Heroku Config Vars:
 *   ADMIN1_USERNAME, ADMIN1_EMAIL, ADMIN1_PASSWORD  ...  ADMIN5_USERNAME, ADMIN5_EMAIL, ADMIN5_PASSWORD
 * Safe to run on every start: an existing admin is never duplicated and its password is NOT changed,
 * unless ADMIN_RESET_PASSWORDS=true is set.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final int MAX_ADMINS = 5;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Environment env;

    public AdminSeeder(UserRepository userRepository, RoleRepository roleRepository,
                       BCryptPasswordEncoder passwordEncoder, Environment env) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean resetPasswords = "true".equalsIgnoreCase(env.getProperty("ADMIN_RESET_PASSWORDS", "false").trim());
        Role adminRole = null;

        for (int i = 1; i <= MAX_ADMINS; i++) {
            String username = clean(env.getProperty("ADMIN" + i + "_USERNAME"));
            String email = clean(env.getProperty("ADMIN" + i + "_EMAIL"));
            String password = env.getProperty("ADMIN" + i + "_PASSWORD");

            if (username == null && email == null && (password == null || password.isEmpty())) {
                continue; // this slot is not configured
            }
            if (username == null || email == null) {
                log.warn("ADMIN{} skipped: ADMIN{}_USERNAME and ADMIN{}_EMAIL are both required", i, i, i);
                continue;
            }

            try {
                if (adminRole == null) {
                    adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
                }
                seedOne(i, username, email, password, adminRole, resetPasswords);
            } catch (Exception e) {
                log.error("ADMIN{} could not be created: {}", i, e.getMessage(), e);
            }
        }
    }

    private void seedOne(int slot, String username, String email, String password, Role adminRole, boolean resetPasswords) {
        if (username.length() > 50 || email.length() > 100) {
            log.warn("ADMIN{} skipped: username max 50 and email max 100 characters", slot);
            return;
        }

        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            boolean changed = false;
            if (!user.hasRole("ADMIN")) {
                user.addRole(adminRole);
                changed = true;
            }
            if (!"ADMIN".equals(user.getRole())) {
                user.setRole("ADMIN");
                changed = true;
            }
            if (!user.isActive()) {
                user.setActive(true);
                changed = true;
            }
            if (resetPasswords && validPassword(password)) {
                user.setPassword(passwordEncoder.encode(password));
                changed = true;
                log.info("ADMIN{}: password reset", slot);
            }
            if (changed) {
                userRepository.save(user);
                log.info("ADMIN{}: existing account {} updated", slot, email);
            } else {
                log.info("ADMIN{}: {} already exists", slot, email);
            }
            return;
        }

        if (userRepository.existsByName(username)) {
            log.warn("ADMIN{} skipped: username '{}' is already used by another account", slot, username);
            return;
        }
        if (!validPassword(password)) {
            log.warn("ADMIN{} skipped: ADMIN{}_PASSWORD must be at least 8 characters", slot, slot);
            return;
        }

        User admin = User.builder()
                .name(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("ADMIN")
                .active(true)
                .createdAt(new Date())
                .build();
        admin.addRole(adminRole);
        userRepository.save(admin);
        log.info("ADMIN{}: account created for {}", slot, email);
    }

    private static boolean validPassword(String password) {
        return password != null && password.length() >= 8;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
