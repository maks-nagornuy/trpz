package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService; // 1. Імпортуємо СЕРВІС

@Controller
@RequestMapping("/user")
public class UserController {

    // 2. Впроваджуємо UserService, а НЕ UserRepository
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Сторінка ЛОГІНУ (GET /user/login)
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Повертає 'templates/login.html'
    }

    /**
     * Сторінка РЕЄСТРАЦІЇ (GET /user/create)
     */
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "create"; // Повертає 'templates/create.html'
    }

    /**
     * Обробка РЕЄСТРАЦІЇ (POST /user/create)
     */
    @PostMapping("/create")
    public String createUserSubmit(@ModelAttribute("user") User user) {
        // 3. Викликаємо сервіс, який хешує пароль
        userService.register(user);
        return "redirect:/user/login"; // Перенаправляємо на логін
    }
}
