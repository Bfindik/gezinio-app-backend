package com.example.gezinio.excursion.repository;

import com.example.gezinio.excursion.model.Transfer;
import com.example.gezinio.excursion.model.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByTourId(Long tourId);

    List<Transfer> findByTransferType(TransferType transferType);

    List<Transfer> findByTourIdAndTransferType(Long tourId, TransferType transferType);
}