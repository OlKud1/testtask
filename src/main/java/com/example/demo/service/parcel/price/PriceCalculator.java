package com.example.demo.service.parcel.price;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.model.DeliveryType;

public interface PriceCalculator {

    double calculatePrice(CreateParcelRequest request);

    DeliveryType getDeliveryType();
}
