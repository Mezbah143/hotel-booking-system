package com.finalproject.hotelbooking.repository;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Booking;
import com.finalproject.hotelbooking.model.BookingStatus;
import com.finalproject.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCreatedAtDesc(AppUser user);

    boolean existsByRoom(Room room);

    boolean existsByRoomAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
            Room room,
            BookingStatus status,
            LocalDate requestedCheckOut,
            LocalDate requestedCheckIn
    );
}
