package com.finalproject.hotelbooking.service;

import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.model.RoomSearchCriteria;
import com.finalproject.hotelbooking.model.BookingStatus;
import com.finalproject.hotelbooking.repository.BookingRepository;
import com.finalproject.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> findAvailableRooms(RoomSearchCriteria criteria) {
        RoomSearchCriteria safeCriteria = criteria == null ? new RoomSearchCriteria() : criteria;
        validateCriteria(safeCriteria);
        return roomRepository.searchAvailable(
                normalize(safeCriteria.getKeyword()),
                normalize(safeCriteria.getCity()),
                normalize(safeCriteria.getRoomType()),
                safeCriteria.getGuests(),
                safeCriteria.getMaxPrice(),
                safeCriteria.getCheckInDate(),
                safeCriteria.getCheckOutDate(),
                BookingStatus.BOOKED
        );
    }

    public List<String> findAvailableCities() {
        return roomRepository.findAvailableCities();
    }

    public List<String> findAvailableRoomTypes() {
        return roomRepository.findAvailableRoomTypes();
    }

    public List<Room> findAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoom(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room was not found."));
    }

    @Transactional
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Transactional
    public void delete(Long id) {
        Room room = getRoom(id);
        if (bookingRepository.existsByRoom(room)) {
            room.setAvailable(false);
            roomRepository.save(room);
            return;
        }
        roomRepository.delete(room);
    }

    private void validateCriteria(RoomSearchCriteria criteria) {
        if (criteria.getGuests() != null && criteria.getGuests() < 1) {
            throw new IllegalArgumentException("Guest count must be at least 1.");
        }
        if (criteria.getMaxPrice() != null && criteria.getMaxPrice().signum() <= 0) {
            throw new IllegalArgumentException("Maximum price must be greater than 0.");
        }
        boolean onlyOneDate = (criteria.getCheckInDate() == null) != (criteria.getCheckOutDate() == null);
        if (onlyOneDate) {
            throw new IllegalArgumentException("Select both check-in and check-out dates.");
        }
        if (criteria.getCheckInDate() != null && !criteria.getCheckOutDate().isAfter(criteria.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (criteria.getCheckInDate() != null && criteria.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
