package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.service.BookingService;
import com.finalproject.hotelbooking.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;

    public BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/bookings")
    public String create(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = userService.getByEmail(authentication.getName());
        try {
            bookingService.createBooking(user, roomId, checkInDate, checkOutDate);
            redirectAttributes.addFlashAttribute("success", "Booking created successfully.");
            return "redirect:/bookings";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/rooms/" + roomId;
        }
    }

    @GetMapping("/bookings")
    public String myBookings(Authentication authentication, Model model) {
        AppUser user = userService.getByEmail(authentication.getName());
        model.addAttribute("bookings", bookingService.findUserBookings(user));
        return "bookings/list";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = userService.getByEmail(authentication.getName());
        try {
            bookingService.cancelBooking(id, user);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/bookings";
    }
}
