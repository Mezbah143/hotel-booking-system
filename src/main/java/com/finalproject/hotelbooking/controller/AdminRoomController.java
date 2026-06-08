package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.service.FileStorageService;
import com.finalproject.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private static final String DEFAULT_IMAGE = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1000&q=80";

    private final RoomService roomService;
    private final FileStorageService fileStorageService;

    public AdminRoomController(RoomService roomService, FileStorageService fileStorageService) {
        this.roomService = roomService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("rooms", roomService.findAllRooms());
        return "admin/rooms";
    }

    @GetMapping("/new")
    public String newRoom(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("formTitle", "Add Room");
        return "admin/room-form";
    }

    @GetMapping("/{id}/edit")
    public String editRoom(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getRoom(id));
        model.addAttribute("formTitle", "Edit Room");
        return "admin/room-form";
    }

    @PostMapping
    public String saveRoom(
            @Valid @ModelAttribute("room") Room room,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", room.getId() == null ? "Add Room" : "Edit Room");
            return "admin/room-form";
        }
        String uploadedPath = fileStorageService.store(imageFile);
        if (uploadedPath != null) {
            room.setImagePath(uploadedPath);
        } else if (room.getImagePath() == null || room.getImagePath().isBlank()) {
            room.setImagePath(DEFAULT_IMAGE);
        }
        roomService.save(room);
        redirectAttributes.addFlashAttribute("success", "Room saved.");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Room deleted or hidden if it already had bookings.");
        return "redirect:/admin/rooms";
    }
}
