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
    public Booking createBooking(
            AppUser user,
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer guestCount,
            String specialRequest
    ) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room was not found."));
        validateBooking(room, checkInDate, checkOutDate, guestCount);

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        booking.setGuestCount(guestCount);
        booking.setSpecialRequest(normalizeSpecialRequest(specialRequest));
        booking.setStatus(BookingStatus.BOOKED);
        return bookingRepository.save(booking);
    }

    public List<Booking> findUserBookings(AppUser user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Booking getUserBooking(Long bookingId, AppUser user) {
        return bookingRepository.findByIdAndUser(bookingId, user)
                .orElseThrow(() -> new IllegalArgumentException("Booking was not found."));
    }

    public List<Booking> findAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public long countAllBookings() {
        return bookingRepository.count();
    }

    public long countActiveBookings() {
        return bookingRepository.countByStatus(BookingStatus.BOOKED);
    }

    @Transactional
    public void cancelBooking(Long bookingId, AppUser user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking was not found."));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only cancel your own bookings.");
        }
        cancelActiveBooking(booking);
    }

    @Transactional
    public void cancelBookingAsAdmin(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking was not found."));
        cancelActiveBooking(booking);
    }

    private void validateBooking(Room room, LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount) {
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
        if (guestCount == null || guestCount < 1) {
            throw new IllegalArgumentException("Guest count must be at least 1.");
        }
        if (guestCount > room.getCapacity()) {
            throw new IllegalArgumentException("This room allows up to " + room.getCapacity() + " guests.");
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

    private void cancelActiveBooking(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("This booking is already cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }

    private String normalizeSpecialRequest(String specialRequest) {
        if (specialRequest == null || specialRequest.isBlank()) {
            return null;
        }
        String normalized = specialRequest.trim();
        if (normalized.length() > 500) {
            throw new IllegalArgumentException("Special request must be 500 characters or fewer.");
        }
        return normalized;
    }
}
