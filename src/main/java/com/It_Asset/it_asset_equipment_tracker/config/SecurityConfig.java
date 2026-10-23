
package com.It_Asset.it_asset_equipment_tracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            ObjectMapper objectMapper)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authenticationProvider(authenticationProvider)

            .exceptionHandling(exceptions -> exceptions

                // Unauthenticated requests receive HTTP 401.
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("timestamp", LocalDateTime.now().toString());
                    body.put("status", 401);
                    body.put("message",
                        "Authentication is required to access this resource");
                    body.put("path", request.getRequestURI());

                    objectMapper.writeValue(
                        response.getOutputStream(), body);
                })

                // Authenticated requests without permission receive HTTP 403.
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("timestamp", LocalDateTime.now().toString());
                    body.put("status", 403);
                    body.put("message",
                        "You do not have permission to access this resource");
                    body.put("path", request.getRequestURI());

                    objectMapper.writeValue(
                        response.getOutputStream(), body);
                })
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/api/health",
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Category endpoints.
                .requestMatchers(HttpMethod.GET, "/api/categories")
                    .hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/categories")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**")
                    .hasRole("ADMIN")

                // Asset endpoints.
                .requestMatchers(HttpMethod.GET, "/api/assets/**")
                    .hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/assets")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/assets/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/assets/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/assets/**")
                    .hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
