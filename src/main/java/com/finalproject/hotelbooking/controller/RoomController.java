package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

@Controller
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getRoom(id));
        model.addAttribute("today", LocalDate.now());
        return "rooms/detail";
    }
}
