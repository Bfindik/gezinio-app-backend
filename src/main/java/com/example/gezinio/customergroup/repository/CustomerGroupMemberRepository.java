package com.example.gezinio.customergroup.repository;

import com.example.gezinio.customergroup.model.CustomerGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerGroupMemberRepository extends JpaRepository<CustomerGroupMember, Long> {

    Optional<CustomerGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);
}
