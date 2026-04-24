package com.nammametro.controller;

import com.nammametro.model.User;
import com.nammametro.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles authentication requests: registration, login, and logout.
 *
 * SRP: This class has one responsibility — routing authentication-related
 *      web requests and delegating business logic to AuthService.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==================== REGISTER ====================

    /**
     * Shows the registration form.
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "auth/register";
    }

    /**
     * POST /auth/register — registers a new user based on role.
     * Uses the Factory Pattern internally via AuthService.
     */
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String role,
                           RedirectAttributes redirectAttributes) {
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/auth/register";
        }

        // Validate password strength
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error",
                    "Password must be at least 6 characters.");
            return "redirect:/auth/register";
        }

        try {
            authService.register(name, email, password, role);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    // ==================== LOGIN ====================

    /**
     * Shows the login form.
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String success,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "auth/login";
    }

    /**
     * POST /auth/login — authenticates user and issues JWT in an HTTP-only cookie.
     * Redirects to role-specific dashboard on success.
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(email, password);

            // Store JWT in an HTTP-only cookie (not accessible via JavaScript)
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);

            // Determine role for dashboard redirect
            String role = extractRoleFromToken(token);
            return switch (role) {
                case "PASSENGER" -> "redirect:/passenger/dashboard";
                case "OPERATOR"  -> "redirect:/operator/dashboard";
                case "ADMIN"     -> "redirect:/admin/dashboard";
                default          -> "redirect:/";
            };

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Invalid email or password. Please try again.");
            return "redirect:/auth/login";
        }
    }

    // ==================== LOGOUT ====================

    /**
     * GET /auth/logout — clears the JWT cookie and redirects to login.
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // immediately expire
        response.addCookie(jwtCookie);

        redirectAttributes.addFlashAttribute("success",
                "You have been logged out successfully.");
        return "redirect:/auth/login";
    }

    // ==================== Helper ====================

    /**
     * Quick helper to extract role from a JWT token for redirect logic.
     * We parse the second part (payload) of the JWT to read the role claim.
     */
    private String extractRoleFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            // Simple extraction — look for "role":"VALUE"
            int idx = payload.indexOf("\"role\"");
            if (idx >= 0) {
                int start = payload.indexOf("\"", idx + 6) + 1;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
        } catch (Exception ignored) {}
        return "PASSENGER"; // fallback
    }
}
