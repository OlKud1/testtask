package com.example.demo.service.parcel.price;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.model.DeliveryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EconomDeliveryPriceCalculator implements PriceCalculator {

    @Override
    public double calculatePrice(CreateParcelRequest request) {
        if (request.getDeliveryType() != DeliveryType.ECONOM) {
            throw new IllegalArgumentException("DeliveryType must be ECONOM");
        }
        return request.getWeight() * 0.1 + 200;
    }

    @Override
    public DeliveryType getDeliveryType() {
        return DeliveryType.ECONOM;
    }
}
