package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.Room;
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
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
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
        try {
            preserveOrStoreImage(room, imageFile);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("imageData", "image", exception.getMessage());
            model.addAttribute("formTitle", room.getId() == null ? "Add Room" : "Edit Room");
            return "admin/room-form";
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

    private void preserveOrStoreImage(Room room, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Please upload a valid image file.");
            }
            if (imageFile.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException("Room image must be 5 MB or smaller.");
            }
            try {
                room.setImageData(imageFile.getBytes());
                room.setImageContentType(contentType);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Could not read the uploaded image.");
            }
            return;
        }

        if (room.getId() != null) {
            Room existing = roomService.getRoom(room.getId());
            room.setImageData(existing.getImageData());
            room.setImageContentType(existing.getImageContentType());
            if (room.getImagePath() == null || room.getImagePath().isBlank()) {
                room.setImagePath(existing.getImagePath());
            }
        }

        if (!room.hasStoredImage() && (room.getImagePath() == null || room.getImagePath().isBlank())) {
            room.setImagePath(DEFAULT_IMAGE);
        }
    }
}
