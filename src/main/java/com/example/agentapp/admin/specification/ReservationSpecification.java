package com.example.agentapp.admin.specification;

import com.example.agentapp.admin.dto.ReservationFilterRequest;
import com.example.agentapp.reservation.model.Reservation;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationSpecification {

    private ReservationSpecification() {}

    public static Specification<Reservation> fromFilter(ReservationFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            ReservationStatus status = filter.getStatus();
            if (status != null) {
                predicates.add(cb.equal(root.get("reservationStatus"), status));
            }

            ReservationType type = filter.getType();
            if (type != null) {
                predicates.add(cb.equal(root.get("reservationType"), type));
            }

            Long userId = filter.getUserId();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            Long tourId = filter.getTourId();
            if (tourId != null) {
                predicates.add(cb.equal(root.get("tour").get("id"), tourId));
            }

            LocalDateTime startDate = filter.getStartDate();
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            LocalDateTime endDate = filter.getEndDate();
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
