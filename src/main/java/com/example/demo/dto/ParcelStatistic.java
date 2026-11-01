package com.example.demo.dto;

import com.example.demo.model.DeliveryType;
import com.example.demo.model.Parcel;
import com.example.demo.model.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class ParcelStatistic {
    private long totalParcels;
    private double averageWeight;
    private double averagePrice;
    private Map<ParcelStatus, Long> parcelsCountByStatus;
    private Map<DeliveryType, Long> parcelsCountByDeliveryType;
    private Parcel mostExpensiveParcel;
    private Parcel cheapestParcel;
    private Parcel heaviestParcel;
    private Parcel lightestParcel;
}
