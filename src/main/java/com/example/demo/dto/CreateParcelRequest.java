package com.example.demo.dto;

import com.example.demo.model.DeliveryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParcelRequest {
    private String sender;
    private String recipient;
    private double weight;
    private DeliveryType deliveryType;
}
