package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.RoomSearchCriteria;
import com.finalproject.hotelbooking.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.time.LocalDate;

@Controller
public class HomeController {

    private final RoomService roomService;

    public HomeController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rooms", roomService.findAvailableRooms(new RoomSearchCriteria()).stream().limit(3).toList());
        model.addAttribute("today", LocalDate.now());
        return "home";
    }

    @GetMapping("/rooms")
    public String rooms(@ModelAttribute("search") RoomSearchCriteria search, Model model) {
        model.addAttribute("cities", roomService.findAvailableCities());
        model.addAttribute("roomTypes", roomService.findAvailableRoomTypes());
        model.addAttribute("today", LocalDate.now());
        try {
            model.addAttribute("rooms", roomService.findAvailableRooms(search));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("searchError", exception.getMessage());
            model.addAttribute("rooms", List.of());
        }
        return "rooms/list";
    }
}
