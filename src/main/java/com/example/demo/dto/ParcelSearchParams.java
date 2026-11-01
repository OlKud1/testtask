package com.example.demo.dto;

import com.example.demo.model.DeliveryType;
import com.example.demo.model.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelSearchParams {
    private String trackingNumber;
    private String sender;
    private String recipient;
    private Double fromWeight;
    private Double toWeight;
    private Double fromPrice;
    private Double toPrice;
    @Builder.Default
    private List<ParcelStatus> statuses = new ArrayList<>();
    @Builder.Default
    private List<DeliveryType> deliveryTypes = new ArrayList<>();
}
