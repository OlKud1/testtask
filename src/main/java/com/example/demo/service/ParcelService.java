package com.example.demo.service;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.model.Parcel;
import com.example.demo.dto.ParcelSearchParams;
import com.example.demo.dto.ParcelStatistic;
import com.example.demo.model.ParcelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParcelService {

    Parcel getByTrackingNumber(String trackingNumber);

    Page<Parcel> findAll(ParcelSearchParams params, Pageable pageable);

    ParcelStatistic buildStatistic(ParcelSearchParams params);

    Parcel create(CreateParcelRequest request);

    Parcel updateStatus(String trackingNumber, ParcelStatus status);
}
