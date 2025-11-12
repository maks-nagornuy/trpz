package com.example.demo.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // 1. Імпорт
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Створюємо бін, який "знає" про ваш UserDetailsService
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Вказуємо ваш сервіс
        authProvider.setPasswordEncoder(passwordEncoder()); // Вказуємо шифратор
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/archive/**",    // Дозволяємо архіви
                        "/user/create",   // Дозволяємо сторінку реєстрації
                        "/user/login"     // Дозволяємо сторінку логіну
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            	    .loginPage("/user/login")
            	    .loginProcessingUrl("/user/login")
            	    .usernameParameter("email")
            	    .passwordParameter("password")
            	    .defaultSuccessUrl("/archive/archives", true)
            	    .permitAll()
            	)
            	.logout(logout -> logout
            	    .logoutUrl("/logout")
            	    .logoutSuccessUrl("/user/login?logout")
            	    .permitAll()
            	);

        // 3. Кажемо Spring Security використовувати ваш бін
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}