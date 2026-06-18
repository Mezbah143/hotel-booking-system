package com.finalproject.hotelbooking.controller;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Booking;
import com.finalproject.hotelbooking.model.BookingStatus;
import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.repository.BookingRepository;
import com.finalproject.hotelbooking.repository.RoomRepository;
import com.finalproject.hotelbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityAndImageTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private RoomRepository roomRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    private Room room;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        room = new Room();
        room.setName("Image Room");
        room.setCity("Seoul");
        room.setRoomType("Deluxe");
        room.setPricePerNight(new BigDecimal("100.00"));
        room.setCapacity(2);
        room.setDescription("Room with stored image");
        room.setAvailable(true);
        room.setImageContentType("image/png");
        room.setImageData(new byte[]{1, 2, 3, 4});
        room = roomRepository.save(room);

        AppUser user = new AppUser();
        user.setName("Viewer");
        user.setEmail("viewer@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(LocalDate.now().plusDays(2));
        booking.setCheckOutDate(LocalDate.now().plusDays(4));
        booking.setGuestCount(2);
        booking.setTotalPrice(new BigDecimal("200.00"));
        booking.setStatus(BookingStatus.BOOKED);
        booking = bookingRepository.save(booking);
    }

    @Test
    void servesStoredRoomImagePublicly() throws Exception {
        mockMvc.perform(get("/rooms/{id}/image", room.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4}));
    }

    @Test
    void publicRoomPagesRender() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rooms/{id}", room.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsRedirectedFromAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "viewer@example.com", roles = "USER")
    void bookingOwnerCanRenderBookingDetails() throws Exception {
        mockMvc.perform(get("/bookings/{id}", booking.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/rooms"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/rooms/new"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk());
    }
}
