package com.example.skillswap.config;

import com.example.skillswap.service.impl.CustomOAuth2UserService;
import com.example.skillswap.service.impl.CustomOidcUserService;
import com.example.skillswap.service.impl.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final UserDetailsService userDetailsService;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final CustomOidcUserService customOidcUserService;
        private final ChatService chatService;

        public SecurityConfig(
                        UserDetailsService userDetailsService,
                        CustomOAuth2UserService customOAuth2UserService,
                        CustomOidcUserService customOidcUserService,
                        ChatService chatService) {
                this.userDetailsService = userDetailsService;
                this.customOAuth2UserService = customOAuth2UserService;
                this.customOidcUserService = customOidcUserService;
                this.chatService = chatService;
        }

        @Bean
        public static BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/css/**", "/js/**", "/img/**", "/fonts/**",
                                                                "/Source/**",
                                                                "/index", "/contact", "/ws-native/**",
                                                                "/oauth2/**",
                                                                "/register", "/register/**",
                                                                "/login",
                                                                "/error/**",
                                                                "/ws/**", "/api/chat/**",
                                                                "/announces-list", "/announce-details", "/meeting")
                                                .permitAll()
                                                .requestMatchers("/ws-native/**").permitAll()
                                                .requestMatchers("/ws/**").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .failureHandler((request, response, exception) -> {

                                                        String message = "Email sau parola incorectă";

                                                        if (exception instanceof BadCredentialsException &&
                                                                        exception.getMessage().contains("Google")) {
                                                                message = exception.getMessage();
                                                        }

                                                        response.sendRedirect(
                                                                        "/login?errorMessage=" +
                                                                                        URLEncoder.encode(message,
                                                                                                        StandardCharsets.UTF_8));
                                                })
                                                .defaultSuccessUrl("/index", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        if (authentication != null) {
                                                                try {
                                                                        Long userId = chatService.getCurrentUserId(authentication);
                                                                        chatService.setUserOffline(userId);
                                                                } catch (Exception ignored) {
                                                                        // Ignore principals that cannot be resolved to an application user.
                                                                }
                                                        }

                                                        response.sendRedirect("/login?logout=true");
                                                })
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService)
                                                                .oidcUserService(customOidcUserService))
                                                .defaultSuccessUrl("/index", true))
                                .oauth2Client(Customizer.withDefaults())
                                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));

                return http.build();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                auth
                                .userDetailsService(userDetailsService)
                                .passwordEncoder(passwordEncoder());
        }
}
