package com.pcd.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.pcd.user.Permission.*;
import static com.pcd.user.Roles.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**",
            "/v2/api-docs",
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(WHITE_LIST_URL)
                                .permitAll()

                                // Admin permissions
                                .requestMatchers("/api/v1/admin/**").hasAnyRole(Admin.name())
                                .requestMatchers(GET, "/api/v1/admin/users").hasAnyAuthority(ADMIN_READ.name())
                                .requestMatchers(POST, "/api/v1/admin/users").hasAnyAuthority(ADMIN_CREATE.name())
                                .requestMatchers(PUT, "/api/v1/admin/users/{id}").hasAnyAuthority(ADMIN_UPDATE.name())
                                .requestMatchers(DELETE, "/api/v1/admin/users/{id}").hasAnyAuthority(ADMIN_DELETE.name())

                                // Expert permissions
                                .requestMatchers("/api/v1/expert/**").hasAnyRole(Expert.name())
                                .requestMatchers(GET, "/api/v1/expert/report").hasAnyAuthority(EXPERT_REPORT.name())
                                .requestMatchers(POST, "/api/v1/expert/upload").hasAnyAuthority(EXPERT_UPLOAD.name())
                                .requestMatchers(POST, "/api/v1/expert/analyse").hasAnyAuthority(EXPERT_ANALYZE.name())
                                .requestMatchers(POST, "/api/v1/expert/annotate").hasAnyAuthority(EXPERT_ANNOTATE.name())

                                // Investigator permissions
                                .requestMatchers("/api/v1/investigator/**").hasAnyRole(Investigator.name())
                                .requestMatchers(GET, "/api/v1/investigator").hasAnyAuthority(INVESTIGATOR_READ.name())
                                .requestMatchers(POST, "/api/v1/investigator").hasAnyAuthority(INVESTIGATOR_SUBMIT.name())

                                // Lawyer permissions
                                .requestMatchers("/api/v1/lawyer/**").hasAnyRole(Lawyer.name())
                                .requestMatchers(GET, "/api/v1/lawyer").hasAnyAuthority(LAWYER_READ.name())
                                .requestMatchers(POST, "/api/v1/lawyer").hasAnyAuthority(LAWYER_SUBMIT.name())
                                .requestMatchers(GET, "/api/v1/lawyer/export").hasAnyAuthority(LAWYER_EXPORT.name())

                                // Judge permissions
                                .requestMatchers("/api/v1/judge/**").hasAnyRole(Judge.name())
                                .requestMatchers(GET, "/api/v1/judge/history").hasAnyAuthority(JUDGE_HISTORY.name())
                                .requestMatchers(GET, "/api/v1/judge/reports").hasAnyAuthority(JUDGE_READ.name())

                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout")
                                .addLogoutHandler(logoutHandler)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                )
        ;

        return http.build();
    }
}