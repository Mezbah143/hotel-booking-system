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
            @RequestParam Integer guestCount,
            @RequestParam(required = false) String specialRequest,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = userService.getByEmail(authentication.getName());
        try {
            var booking = bookingService.createBooking(
                    user, roomId, checkInDate, checkOutDate, guestCount, specialRequest);
            redirectAttributes.addFlashAttribute("success", "Booking created successfully.");
            return "redirect:/bookings/" + booking.getId();
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

    @GetMapping("/bookings/{id}")
    public String bookingDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        AppUser user = userService.getByEmail(authentication.getName());
        try {
            model.addAttribute("booking", bookingService.getUserBooking(id, user));
            return "bookings/detail";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/bookings";
        }
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
