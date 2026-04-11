package com.example.agentapp.excursion.repository;

import com.example.agentapp.excursion.model.Room;
import com.example.agentapp.excursion.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    List<Room> findByHotelIdAndAvailable(Long hotelId, boolean available);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByHotelIdAndRoomType(Long hotelId, RoomType roomType);
}