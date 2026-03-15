package com.example.skillswap.config;

import com.example.skillswap.service.ChatService;
import com.example.skillswap.service.OAuthUserService;
import com.example.skillswap.service.OidcUserService;
import com.example.skillswap.service.UserAccessService;
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
        private final OAuthUserService customOAuth2UserService;
        private final OidcUserService customOidcUserService;
        private final ChatService chatService;
        private final UserAccessService userAccessService;

        public SecurityConfig(
                        UserDetailsService userDetailsService,
                        OAuthUserService customOAuth2UserService,
                        OidcUserService customOidcUserService,
                        ChatService chatService,
                        UserAccessService userAccessService) {
                this.userDetailsService = userDetailsService;
                this.customOAuth2UserService = customOAuth2UserService;
                this.customOidcUserService = customOidcUserService;
                this.chatService = chatService;
                this.userAccessService = userAccessService;
        }

        @Bean
        public static BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                        .requiresChannel(channel ->
                                channel.anyRequest().requiresSecure()
                        )
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(authorize -> authorize
                                        .requestMatchers(
                                                "/",
                                                "/index",
                                                "/announces-list",
                                                "/announce-details/**",
                                                "/profile/**",
                                                "/css/**",
                                                "/js/**",
                                                "/img/**",
                                                "/fonts/**",
                                                "/login",
                                                "/register",
                                                "/oauth2/**",
                                                "/privacy",
                                                "/terms-of-service",
                                                "/privacy-policy")
                                                .permitAll()
                                                .requestMatchers("/ws-native/**").permitAll()
                                                .requestMatchers("/ws/**").permitAll()
                                                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .failureHandler((request, response, exception) -> {

                                                        String message = "Email sau parola incorectă";

                                                        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
                                                                message = exception.getMessage();
                                                        }

                                                        response.sendRedirect(
                                                                        "/login?errorMessage=" +
                                                                                        URLEncoder.encode(message,
                                                                                                        StandardCharsets.UTF_8));
                                                })
                                                .successHandler((request, response, authentication) -> {
                                                        userAccessService.recordSuccessfulLogin(authentication.getName());
                                                        response.sendRedirect("/index");
                                                })
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
                                                .successHandler((request, response, authentication) -> {
                                                        userAccessService.recordSuccessfulLogin(authentication.getName());
                                                        response.sendRedirect("/index");
                                                }))
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
