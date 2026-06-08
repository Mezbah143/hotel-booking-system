package com.finalproject.hotelbooking.service;

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
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private AppUser user;
    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        user = new AppUser();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        room = new Room();
        room.setName("Test Room");
        room.setCity("Seoul");
        room.setRoomType("Deluxe");
        room.setPricePerNight(new BigDecimal("100.00"));
        room.setCapacity(2);
        room.setDescription("A clean test room.");
        room.setImagePath("https://example.com/room.jpg");
        room.setAvailable(true);
        room = roomRepository.save(room);
    }

    @Test
    void createsBookingWithTotalPrice() {
        Booking booking = bookingService.createBooking(
                user,
                room.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4)
        );

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.BOOKED);
        assertThat(booking.getTotalPrice()).isEqualByComparingTo("300.00");
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThatThrownBy(() -> bookingService.createBooking(
                user,
                room.getId(),
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(2)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    void rejectsOverlappingBooking() {
        bookingService.createBooking(
                user,
                room.getId(),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(5)
        );

        assertThatThrownBy(() -> bookingService.createBooking(
                user,
                room.getId(),
                LocalDate.now().plusDays(4),
                LocalDate.now().plusDays(6)
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void cancelsOwnBooking() {
        Booking booking = bookingService.createBooking(
                user,
                room.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        bookingService.cancelBooking(booking.getId(), user);

        Booking cancelled = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
}
