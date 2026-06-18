package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.repository.RoomRepository;
import com.finalproject.hotelbooking.repository.UserRepository;
import com.finalproject.hotelbooking.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public AdminController(
            RoomRepository roomRepository,
            UserRepository userRepository,
            BookingService bookingService
    ) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalRooms", roomRepository.count());
        model.addAttribute("availableRooms", roomRepository.countByAvailableTrue());
        model.addAttribute("totalBookings", bookingService.countAllBookings());
        model.addAttribute("activeBookings", bookingService.countActiveBookings());
        model.addAttribute("registeredUsers", userRepository.count());
        model.addAttribute("recentBookings", bookingService.findAllBookings().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(Model model) {
        model.addAttribute("bookings", bookingService.findAllBookings());
        return "admin/bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBookingAsAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}
