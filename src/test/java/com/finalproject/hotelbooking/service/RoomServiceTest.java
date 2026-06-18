package com.finalproject.hotelbooking.service;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.model.RoomSearchCriteria;
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
class RoomServiceTest {

    @Autowired private RoomService roomService;
    @Autowired private BookingService bookingService;
    @Autowired private RoomRepository roomRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    private Room seoulRoom;
    private Room busanRoom;
    private AppUser user;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        userRepository.deleteAll();

        seoulRoom = roomRepository.save(room("Seoul Suite", "Seoul", "Suite", "180.00", 3));
        busanRoom = roomRepository.save(room("Busan Standard", "Busan", "Standard", "80.00", 2));

        user = new AppUser();
        user.setName("Search User");
        user.setEmail("search@example.com");
        user.setPassword("password");
        user = userRepository.save(user);
    }

    @Test
    void filtersByCityGuestsAndPrice() {
        RoomSearchCriteria search = new RoomSearchCriteria();
        search.setCity("Busan");
        search.setGuests(2);
        search.setMaxPrice(new BigDecimal("100.00"));

        assertThat(roomService.findAvailableRooms(search))
                .extracting(Room::getName)
                .containsExactly("Busan Standard");
    }

    @Test
    void excludesOverlappingBookingButAllowsAdjacentStay() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(8);
        bookingService.createBooking(user, seoulRoom.getId(), checkIn, checkOut, 2, null);

        RoomSearchCriteria overlap = new RoomSearchCriteria();
        overlap.setCheckInDate(checkIn.plusDays(1));
        overlap.setCheckOutDate(checkOut.plusDays(1));
        assertThat(roomService.findAvailableRooms(overlap))
                .extracting(Room::getId)
                .doesNotContain(seoulRoom.getId());

        RoomSearchCriteria adjacent = new RoomSearchCriteria();
        adjacent.setCheckInDate(checkOut);
        adjacent.setCheckOutDate(checkOut.plusDays(2));
        assertThat(roomService.findAvailableRooms(adjacent))
                .extracting(Room::getId)
                .contains(seoulRoom.getId());
    }

    @Test
    void rejectsIncompleteDatePair() {
        RoomSearchCriteria search = new RoomSearchCriteria();
        search.setCheckInDate(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> roomService.findAvailableRooms(search))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both check-in and check-out");
    }

    private Room room(String name, String city, String type, String price, int capacity) {
        Room room = new Room();
        room.setName(name);
        room.setCity(city);
        room.setRoomType(type);
        room.setPricePerNight(new BigDecimal(price));
        room.setCapacity(capacity);
        room.setDescription("Test room description");
        room.setImagePath("https://example.com/room.jpg");
        room.setAvailable(true);
        return room;
    }
}
