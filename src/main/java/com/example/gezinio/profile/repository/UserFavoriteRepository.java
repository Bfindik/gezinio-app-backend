package com.example.gezinio.profile.repository;

import com.example.gezinio.profile.model.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    List<UserFavorite> findByUserId(Long userId);

    Optional<UserFavorite> findByUserIdAndTourId(Long userId, Long tourId);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    void deleteByUserIdAndTourId(Long userId, Long tourId);

    long countByUserId(Long userId);
}
