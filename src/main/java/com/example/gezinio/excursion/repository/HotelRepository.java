package com.example.gezinio.excursion.repository;

import com.example.gezinio.excursion.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    boolean existsByName(String name);

    List<Hotel> findByActive(boolean active);

    List<Hotel> findByCityIgnoreCase(String city);

    List<Hotel> findByCountryIgnoreCase(String country);

    List<Hotel> findByCityIgnoreCaseAndCountryIgnoreCase(String city, String country);

    List<Hotel> findByStarRating(Integer starRating);

    List<Hotel> findByStarRatingGreaterThanEqual(Integer minStarRating);

    List<Hotel> findByNameContainingIgnoreCase(String name);
}