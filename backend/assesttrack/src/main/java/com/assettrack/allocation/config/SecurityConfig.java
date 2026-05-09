package com.assettrack.allocation.config;

import com.assettrack.allocation.repository.UserRepository;
import com.assettrack.allocation.security.JwtAuthenticationFilter;
import com.assettrack.allocation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }


    /**
     * Main filter chain:
     *  - CSRF disabled  (stateless REST API)
     *  - /api/auth/**   is public (signup, login)
     *  - Everything else requires a valid JWT
     *  - Sessions are STATELESS — no HttpSession is created
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/dev/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Loads users from the database by email and delegates password comparison
     * to BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** Resolve users by email (our username field). */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}


// package com.assettrack.allocation.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
// import org.springframework.security.web.SecurityFilterChain;

// /**
//  * =====================================================================
//  * DEV SECURITY CONFIG — Authentication bypassed for local testing.
//  *
//  * ⚠️  NOTE FOR TEAM:
//  *   This config disables JWT auth so you can test all Allocation
//  *   endpoints freely with Postman / curl / browser without a token.
//  *
//  *   When the Authentication member finishes their work, replace this
//  *   file with the real JWT SecurityFilterChain and remove the
//  *   in-memory users below.
//  *
//  *   The @PreAuthorize annotations on the controller ARE still active
//  *   (EnableMethodSecurity is ON), so you can test role checks using
//  *   the 3 built-in users:
//  *     - admin   / admin123   → ROLE_ADMIN
//  *     - manager / manager123 → ROLE_MANAGER
//  *     - dev     / dev123     → ROLE_DEVELOPER
//  *
//  *   Use Basic Auth in Postman to pick which role you test with.
//  * =====================================================================
//  */
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true)
// public class SecurityConfig {

//     /**
//      * Permit all requests — no JWT filter needed yet.
//      * CSRF disabled for REST API testing.
//      */
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(AbstractHttpConfigurer::disable)
//             .authorizeHttpRequests(auth -> auth
//                 .anyRequest().authenticated()
//             )
//             // Use HTTP Basic Auth so you can switch roles in Postman easily
//             .httpBasic(basic -> {});

//         return http.build();
//     }

//     /**
//      * Three in-memory users mirroring the real role structure.
//      * Remove this bean when real JWT auth is integrated.
//      */
//     @Bean
//     public UserDetailsService inMemoryUsers() {
//         var admin = User.withDefaultPasswordEncoder()
//                 .username("admin")
//                 .password("admin123")
//                 .roles("ADMIN")
//                 .build();

//         var manager = User.withDefaultPasswordEncoder()
//                 .username("manager")
//                 .password("manager123")
//                 .roles("MANAGER")
//                 .build();

//         var developer = User.withDefaultPasswordEncoder()
//                 .username("dev")
//                 .password("dev123")
//                 .roles("DEVELOPER")
//                 .build();

//         return new InMemoryUserDetailsManager(admin, manager, developer);
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
// }
// }


