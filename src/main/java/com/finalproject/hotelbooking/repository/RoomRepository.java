package com.finalproject.hotelbooking.repository;

import com.finalproject.hotelbooking.model.Room;
import com.finalproject.hotelbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailableTrueOrderByCityAscNameAsc();

    long countByAvailableTrue();

    @Query("""
            select r from Room r
            where r.available = true
              and (:keyword is null or
                lower(r.name) like lower(concat('%', :keyword, '%')) or
                lower(r.city) like lower(concat('%', :keyword, '%')) or
                lower(r.roomType) like lower(concat('%', :keyword, '%')) or
                lower(r.description) like lower(concat('%', :keyword, '%')))
              and (:city is null or lower(r.city) = lower(:city))
              and (:roomType is null or lower(r.roomType) = lower(:roomType))
              and (:guests is null or r.capacity >= :guests)
              and (:maxPrice is null or r.pricePerNight <= :maxPrice)
              and (:checkInDate is null or :checkOutDate is null or not exists (
                select b.id from Booking b
                where b.room = r
                  and b.status = :bookedStatus
                  and b.checkInDate < :checkOutDate
                  and b.checkOutDate > :checkInDate))
            order by r.city asc, r.name asc
            """)
    List<Room> searchAvailable(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("roomType") String roomType,
            @Param("guests") Integer guests,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("bookedStatus") BookingStatus bookedStatus
    );

    @Query("select distinct r.city from Room r where r.available = true order by r.city")
    List<String> findAvailableCities();

    @Query("select distinct r.roomType from Room r where r.available = true order by r.roomType")
    List<String> findAvailableRoomTypes();
}
