package com.nammametro.controller;

import com.nammametro.service.NotificationService;
import com.nammametro.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Passenger notification center — view all notifications, mark as read.
 *
 * SRP: This class has one responsibility — handling passenger notification requests.
 */
@Controller
@RequestMapping("/passenger/notifications")
public class PassengerNotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public PassengerNotificationController(NotificationService notificationService,
                                            UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /** View all notifications for the logged-in passenger */
    @GetMapping
    public String listNotifications(Authentication authentication, Model model) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            userService.findByEmail(ud.getUsername()).ifPresent(user -> {
                model.addAttribute("notifications",
                        notificationService.findByUserId(user.getId()));
                model.addAttribute("unreadCount",
                        notificationService.countUnread(user.getId()));
                model.addAttribute("user", user);
            });
        }
        return "passenger/notifications";
    }

    /** Mark a notification as read */
    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        notificationService.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationService.save(n);
        });
        return "redirect:/passenger/notifications";
    }

    /** Mark all notifications as read */
    @PostMapping("/read-all")
    public String markAllAsRead(Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails ud) {
            userService.findByEmail(ud.getUsername()).ifPresent(user -> {
                var notifications = notificationService.findByUserId(user.getId());
                for (var n : notifications) {
                    if (!n.getIsRead()) {
                        n.setIsRead(true);
                        notificationService.save(n);
                    }
                }
            });
        }
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read.");
        return "redirect:/passenger/notifications";
    }
}
