package com.example.agentapp.excursion.repository;

import com.example.agentapp.excursion.model.Tour;
import com.example.agentapp.excursion.model.TourStatus;
import com.example.agentapp.excursion.model.TourType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    boolean existsByName(String name);

    Optional<Tour> findByName(String name);

    Optional<Tour> findByDestination(String destination);

    List<Tour> findByDestinationContainingIgnoreCase(String destination);

    List<Tour> findByTourStatus(TourStatus tourStatus);

    List<Tour> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Tour> findByPriceLessThanEqual(BigDecimal maxPrice);

    List<Tour> findByPriceGreaterThanEqual(BigDecimal minPrice);

    List<Tour> findByDurationDays(Integer days);

    List<Tour> findByDurationDaysLessThanEqual(Integer maxDays);

    List<Tour> findByDurationDaysGreaterThanEqual(Integer minDays);

    List<Tour> findByDestinationAndTourStatus(String destination, TourStatus tourStatus);

    List<Tour> findByTourTypeAndTourStatus(TourType tourType, TourStatus tourStatus);

    List<Tour> findByDestinationContainingIgnoreCaseAndPriceBetweenAndTourStatus(
            String destination,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            TourStatus tourStatus);

    @Query("SELECT t FROM Tour t WHERE " +
            "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND t.tourStatus = 'ACTIVE'")
    List<Tour> searchActiveTours(@Param("keyword") String keyword);

    @Query("SELECT t FROM Tour t WHERE t.tourStatus = 'ACTIVE' ORDER BY t.price ASC")
    List<Tour> findActiveToursOrderByPriceAsc();

    @Query("SELECT t FROM Tour t WHERE t.tourStatus = 'ACTIVE' ORDER BY t.createdAt DESC")
    List<Tour> findRecentActiveTours();

    @Query("SELECT t.destination, COUNT(t) FROM Tour t WHERE t.tourStatus = 'ACTIVE' GROUP BY t.destination")
    List<Object[]> countActiveToursByDestination();

    @Query("SELECT AVG(t.price) FROM Tour t WHERE t.tourStatus = 'ACTIVE'")
    BigDecimal getAverageActiveTourPrice();

    List<Tour> findByCreatedBy(Long userId);

    List<Tour> findByTourStatusOrderByCreatedAtDesc(TourStatus tourStatus);
}