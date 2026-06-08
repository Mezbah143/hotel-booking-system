package com.finalproject.hotelbooking.service;

import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.repository.BookingRepository;
import com.finalproject.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> findAvailableRooms(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return roomRepository.searchAvailable(keyword.trim());
        }
        return roomRepository.findByAvailableTrueOrderByCityAscNameAsc();
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
}
