package com.example.gezinio.customergroup.repository;

import com.example.gezinio.customergroup.model.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {

    boolean existsByGroupCode(String groupCode);

    Optional<CustomerGroup> findByGroupCode(String groupCode);

    long countByGroupCodeStartingWith(String prefix);
}
