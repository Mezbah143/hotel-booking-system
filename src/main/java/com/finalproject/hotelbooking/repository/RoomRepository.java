package com.finalproject.hotelbooking.repository;

import com.finalproject.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByAvailableTrueOrderByCityAscNameAsc();

    @Query("""
            select r from Room r
            where r.available = true
              and (
                lower(r.name) like lower(concat('%', :keyword, '%')) or
                lower(r.city) like lower(concat('%', :keyword, '%')) or
                lower(r.roomType) like lower(concat('%', :keyword, '%')) or
                lower(r.description) like lower(concat('%', :keyword, '%'))
              )
            order by r.city asc, r.name asc
            """)
    List<Room> searchAvailable(@Param("keyword") String keyword);
}
