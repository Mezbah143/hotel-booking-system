package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final RoomService roomService;

    public HomeController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rooms", roomService.findAvailableRooms(null).stream().limit(3).toList());
        return "home";
    }

    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("rooms", roomService.findAvailableRooms(keyword));
        return "rooms/list";
    }
}
