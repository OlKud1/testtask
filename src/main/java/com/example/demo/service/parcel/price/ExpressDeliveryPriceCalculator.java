package com.example.demo.service.parcel.price;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.model.DeliveryType;
import org.springframework.stereotype.Component;

@Component
public class ExpressDeliveryPriceCalculator implements PriceCalculator {

    @Override
    public double calculatePrice(CreateParcelRequest request) {
        if (request.getDeliveryType() != DeliveryType.EXPRESS) {
            throw new IllegalArgumentException("DeliveryType must be EXPRESS");
        }
        return request.getWeight() * 0.3 + 600;
    }

    @Override
    public DeliveryType getDeliveryType() {
        return DeliveryType.EXPRESS;
    }
}
