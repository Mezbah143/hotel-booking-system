package com.finalproject.hotelbooking.service;

import com.finalproject.hotelbooking.model.AppUser;
import com.finalproject.hotelbooking.model.Booking;
import com.finalproject.hotelbooking.model.BookingStatus;
import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.repository.BookingRepository;
import com.finalproject.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public Booking createBooking(AppUser user, Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room was not found."));
        validateBooking(room, checkInDate, checkOutDate);

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        booking.setStatus(BookingStatus.BOOKED);
        return bookingRepository.save(booking);
    }

    public List<Booking> findUserBookings(AppUser user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void cancelBooking(Long bookingId, AppUser user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking was not found."));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only cancel your own bookings.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }

    private void validateBooking(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        if (!room.isAvailable()) {
            throw new IllegalArgumentException("This room is not available.");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("Please select both check-in and check-out dates.");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        boolean overlap = bookingRepository.existsByRoomAndStatusAndCheckInDateLessThanAndCheckOutDateGreaterThan(
                room,
                BookingStatus.BOOKED,
                checkOutDate,
                checkInDate
        );
        if (overlap) {
            throw new IllegalArgumentException("This room is already booked for the selected dates.");
        }
    }
}
