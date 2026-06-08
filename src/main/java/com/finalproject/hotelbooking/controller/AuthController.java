package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") AppUser user,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("success", "Account created. Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
            return "auth/register";
        }
    }
}
