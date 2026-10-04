package com.rukshan.ranaswanu.config;

import com.rukshan.ranaswanu.security.ApiErrorWriter;
import com.rukshan.ranaswanu.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())   // lets CORS run before security
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // no / bad / expired token -> 401 with a readable message
                        .authenticationEntryPoint((req, res, e) ->
                                ApiErrorWriter.write(res, 401, "Please log in again."))
                        .accessDeniedHandler((req, res, e) -> {
                            String uri = req.getRequestURI();
                            String message = uri.startsWith("/api/admin/")
                                    ? "Only administrators can access this resource."
                                    : uri.startsWith("/api/farmer/")
                                    ? "Only farmers can access this resource."
                                    : uri.startsWith("/api/buyer/")
                                    ? "Only buyers, farmers or transport users can access this resource."
                                    : "You do not have permission to do this.";
                            ApiErrorWriter.write(res, 403, message);
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/reviews").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/farmer/**").hasRole("FARMER")
                        .requestMatchers("/api/delivery/vehicles/**").hasRole("TRANSPORT")
                        .requestMatchers("/api/buyer/**").hasAnyRole("BUYER", "FARMER", "TRANSPORT")
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
