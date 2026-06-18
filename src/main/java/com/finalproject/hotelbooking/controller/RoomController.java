package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Controller
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) Integer guests,
            Model model
    ) {
        model.addAttribute("room", roomService.getRoom(id));
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("checkInDate", checkInDate);
        model.addAttribute("checkOutDate", checkOutDate);
        model.addAttribute("guests", guests == null ? 1 : guests);
        return "rooms/detail";
    }

    @GetMapping("/rooms/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        var room = roomService.getRoom(id);
        if (!room.hasStoredImage()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(room.getImageContentType());
        } catch (Exception ignored) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(room.getImageData());
    }
}
