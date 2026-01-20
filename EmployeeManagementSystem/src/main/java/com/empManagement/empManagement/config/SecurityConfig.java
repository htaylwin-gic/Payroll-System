package com.empManagement.empManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder encoder) {
                UserDetails admin = User.withUsername("admin")
                                .password(encoder.encode("admin123"))
                                .roles("ADMIN")
                                .build();

                UserDetails hr = User.withUsername("hr")
                                .password(encoder.encode("hr123"))
                                .roles("HR")
                                .build();

                UserDetails manager = User.withUsername("manager")
                                .password(encoder.encode("manager123"))
                                .roles("MANAGER")
                                .build();

                return new InMemoryUserDetailsManager(admin, hr, manager);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(authorize -> authorize
                                                // Public endpoints
                                                .requestMatchers(
                                                                "/",
                                                                "/home",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/auth/login",
                                                                "/auth/register",
                                                                "/error")
                                                .permitAll()

                                                // Role-based access control
                                                .requestMatchers(
                                                                "/admin/**",
                                                                "/employees/**",
                                                                "/status/**",
                                                                "/payroll/**",
                                                                "/salary/**")
                                                .hasAnyRole("ADMIN", "HR", "MANAGER")

                                                .requestMatchers(
                                                                "/api/admin/**",
                                                                "/system/**")
                                                .hasRole("ADMIN")

                                                // Dashboard should be accessible to authenticated users
                                                .requestMatchers("/dashboard")
                                                .hasAnyRole("ADMIN", "HR", "MANAGER")

                                                // All other requests require authentication
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth/login")
                                                .loginProcessingUrl("/auth/authenticate")
                                                .defaultSuccessUrl("/dashboard", true)
                                                .failureUrl("/auth/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/auth/logout")
                                                .logoutSuccessUrl("/auth/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(exception -> exception
                                                .accessDeniedPage("/auth/access-denied"))
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .expiredUrl("/auth/login?expired=true"));

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}