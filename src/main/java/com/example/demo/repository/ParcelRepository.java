package com.example.demo.repository;

import com.example.demo.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel, Long>,
        JpaSpecificationExecutor<Parcel> {

    Optional<Parcel> findByTrackingNumber(String trackingNumber);
}
