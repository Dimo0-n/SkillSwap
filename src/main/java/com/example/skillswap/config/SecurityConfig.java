package com.example.skillswap.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final UserDetailsService userDetailsService;

        public SecurityConfig(UserDetailsService userDetailsService) {
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public static BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests((authorize) -> authorize
                                .requestMatchers(
                                                "/css/**", "/js/**", "/img/**", "/fonts/**", "/Source/**",
                                                "/index", "/contact", "/register", "/login", "/error/**",
                                                "/announces-list", "/announce-details", "/meeting")
                                .permitAll()
                                .requestMatchers("/register/save").permitAll() // Allow registration
                                .anyRequest().authenticated())
                                .formLogin(
                                                form -> form
                                                                .loginPage("/login")
                                                                .defaultSuccessUrl("/index", true)
                                                                .failureUrl("/login?error")
                                                                .permitAll())
                                .sessionManagement(session -> session
                                        .invalidSessionUrl("/login?expired=true")
                                )
                                .logout(
                                                logout -> logout
                                                                .logoutUrl("/logout")
                                                                .invalidateHttpSession(true) // Închide sesiunea curentă
                                                                .deleteCookies("JSESSIONID") // Șterge cookie-ul
                                                                                             // sesiunii
                                                                .logoutSuccessUrl("/login?logout=true")
                                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .accessDeniedPage("/error/403"));
                return http.build();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth
                                .userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder());
        }

}