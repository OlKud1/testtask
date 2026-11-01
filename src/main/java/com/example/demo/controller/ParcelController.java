package com.example.demo.controller;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.dto.ParcelSearchParams;
import com.example.demo.dto.ParcelStatistic;
import com.example.demo.dto.UpdateParcelStatusRequest;
import com.example.demo.model.Parcel;
import com.example.demo.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    @GetMapping("/{trackingNumber}")
    public Parcel findByTrackingNumber(@PathVariable("trackingNumber") String trackingNumber) {
        return parcelService.getByTrackingNumber(trackingNumber);
    }

    @GetMapping
    public Page<Parcel> findAll(ParcelSearchParams params, Pageable pageable) {
        return parcelService.findAll(params, pageable);
    }

    @GetMapping("/statistic")
    public ParcelStatistic findAll(ParcelSearchParams params) {
        return parcelService.buildStatistic(params);
    }

    @PostMapping
    public Parcel create(@RequestBody CreateParcelRequest request) {
        return parcelService.create(request);
    }

    @PatchMapping("/{trackingNumber}")
    public Parcel updateStatus(@PathVariable("trackingNumber") String trackingNumber,
                               @RequestBody UpdateParcelStatusRequest request) {
        return parcelService.updateStatus(trackingNumber, request.getStatus());
    }
}
