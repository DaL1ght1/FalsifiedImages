package com.pcd.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import static com.pcd.user.enums.Permission.*;
import static com.pcd.user.enums.Role.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // Keep whitelist for potential future use or specific needs like CSRF endpoint
    private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**",
            "/v2/api-docs",
            "/api/v1/auth/csrf-token",
            "/api/v1/api/v1/csrf-token", // Keep CSRF token endpoint accessible
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"};

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final CorsConfigurationSource corsConfigurationSource; // Assuming this is provided via Customizer.withDefaults() now

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Keep CORS config
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))

                .authorizeHttpRequests(req ->
                req.requestMatchers(WHITE_LIST_URL)
                        .permitAll()

                        // Admin permissions
                        .requestMatchers(GET, "/api/v1/admin/users/**").hasAnyAuthority(ADMIN_READ.getPermission())
                        .requestMatchers(POST, "/api/v1/admin/users/**").hasAnyAuthority(ADMIN_CREATE.getPermission())
                        .requestMatchers(PUT, "/api/v1/admin/users/{id}").hasAnyAuthority(ADMIN_UPDATE.getPermission())
                        .requestMatchers(DELETE, "/api/v1/admin/users/{id}").hasAnyAuthority(ADMIN_DELETE.getPermission())
                        .requestMatchers("/api/v1/admin/**").hasAnyRole(ADMIN.name())

                        // Expert permissions
                        .requestMatchers(GET, "/api/v1/expert/report/**").hasAnyAuthority(EXPERT_REPORT.getPermission())
                        .requestMatchers(POST, "/api/v1/expert/upload/**").hasAnyAuthority(EXPERT_UPLOAD.getPermission())
                        .requestMatchers(POST, "/api/v1/expert/analyse/**").hasAnyAuthority(EXPERT_ANALYZE.getPermission())
                        .requestMatchers(POST, "/api/v1/expert/annotate/**").hasAnyAuthority(EXPERT_ANNOTATE.getPermission())
                        .requestMatchers("/api/v1/expert/**").hasAnyRole(EXPERT.name(), ADMIN.name())

                        // Investigator permissions
                        .requestMatchers(GET, "/api/v1/investigator/**").hasAnyAuthority(INVESTIGATOR_READ.getPermission())
                        .requestMatchers(POST, "/api/v1/investigator/**").hasAnyAuthority(INVESTIGATOR_SUBMIT.getPermission())
                        .requestMatchers("/api/v1/investigator/**").hasAnyRole(INVESTIGATOR.name(), ADMIN.name())

                        // Lawyer permissions
                        .requestMatchers(GET, "/api/v1/lawyer/**").hasAnyAuthority(LAWYER_READ.getPermission())
                        .requestMatchers(POST, "/api/v1/lawyer/**").hasAnyAuthority(LAWYER_SUBMIT.getPermission())
                        .requestMatchers(GET, "/api/v1/lawyer/export/**").hasAnyAuthority(LAWYER_EXPORT.getPermission())
                        .requestMatchers("/api/v1/lawyer/**").hasAnyRole(LAWYER.name(), ADMIN.name())

                        // Judge permissions
                        .requestMatchers(GET, "/api/v1/judge/history/**").hasAnyAuthority(JUDGE_HISTORY.getPermission())
                        .requestMatchers(GET, "/api/v1/judge/reports/**").hasAnyAuthority(JUDGE_READ.getPermission())
                        .requestMatchers("/api/v1/judge/**").hasAnyRole(JUDGE.name(), ADMIN.name())


                        .anyRequest()
                        .authenticated()

        )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS)) // Keep stateless
                .authenticationProvider(authenticationProvider) // Keep authentication provider
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // Keep JWT filter (see note above)
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                )
        ;

        return http.build();
    }
}