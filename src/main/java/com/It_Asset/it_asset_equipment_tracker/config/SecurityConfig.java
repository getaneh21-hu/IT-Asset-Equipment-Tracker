
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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

    // Allow requests from the React development frontend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173",
                "http://localhost:5174"
            )
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
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
            // Enable CORS for browser requests.
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())

            // Use stateless JWT authentication.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authenticationProvider(authenticationProvider)

            // Return consistent JSON responses for 401 and 403.
            .exceptionHandling(exceptions -> exceptions

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

                // Public endpoints.
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

                // ADMIN and STAFF can view their assigned assets.
                .requestMatchers(HttpMethod.GET, "/api/assets/my-assets")
                    .hasAnyRole("ADMIN", "STAFF")

                // ADMIN and STAFF can list assets and view an asset by ID.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/assets",
                    "/api/assets/*"
                ).hasAnyRole("ADMIN", "STAFF")

                // Assigning an asset: ADMIN only.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/assets/*/assign/*"
                ).hasRole("ADMIN")

                // Creating an asset: ADMIN only.
                .requestMatchers(HttpMethod.POST, "/api/assets")
                    .hasRole("ADMIN")

                // Updating an asset: ADMIN only.
                .requestMatchers(HttpMethod.PUT, "/api/assets/**")
                    .hasRole("ADMIN")

                // Changing asset status: ADMIN only.
                .requestMatchers(HttpMethod.PATCH, "/api/assets/**")
                    .hasRole("ADMIN")

                // Deleting an asset: ADMIN only.
                .requestMatchers(HttpMethod.DELETE, "/api/assets/**")
                    .hasRole("ADMIN")

                // Require authentication for anything not listed above.
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
