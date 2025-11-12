package com.example.demo.auth;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Потрібен цей імпорт
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List; // Потрібен цей імпорт

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 'username' тут - це те, що користувач ввів у поле. 
        // Ми використовуємо його для пошуку по email.
        
        // ----- ВИПРАВЛЕНО ТУТ -----
        User user = userRepository.findByEmail(username); 
        // -------------------------

        if (user == null) {
            throw new UsernameNotFoundException("Користувача не знайдено: " + username);
        }
        
        // ----- І ВИПРАВЛЕНО ТУТ -----
        // Ми передаємо email як логін і використовуємо роль з вашої моделі
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), // <--- Використовуємо email
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())) // <--- Використовуємо роль
        );
        // -------------------------
    }
}