package com.example.demo.service.parcel.price;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.model.DeliveryType;
import org.springframework.stereotype.Component;

@Component
public class DefaultDeliveryPriceCalculator implements PriceCalculator {

    @Override
    public double calculatePrice(CreateParcelRequest request) {
        if (request.getDeliveryType() != DeliveryType.DEFAULT) {
            throw new IllegalArgumentException("DeliveryType must be DEFAULT");
        }
        return request.getWeight() * 0.2 + 400;
    }

    @Override
    public DeliveryType getDeliveryType() {
        return DeliveryType.DEFAULT;
    }
}
