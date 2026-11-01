package com.example.demo.service;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.dto.ParcelSearchParams;
import com.example.demo.dto.ParcelStatistic;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.DeliveryType;
import com.example.demo.model.Parcel;
import com.example.demo.model.ParcelStatus;
import com.example.demo.repository.ParcelRepository;
import com.example.demo.service.parcel.price.PriceCalculator;
import com.example.demo.util.CommonGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.demo.util.SpecificationUtils.gte;
import static com.example.demo.util.SpecificationUtils.iLike;
import static com.example.demo.util.SpecificationUtils.in;
import static com.example.demo.util.SpecificationUtils.lte;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final CommonGenerator generator;
    private final List<PriceCalculator> priceCalculators; // Spring injects all PriceCalculator beans here

    public ParcelServiceImpl(ParcelRepository parcelRepository,
                             CommonGenerator generator,
                             List<PriceCalculator> priceCalculators) {
        this.parcelRepository = parcelRepository;
        this.generator = generator;
        this.priceCalculators = priceCalculators;
    }

    @Override
    public Parcel getByTrackingNumber(String trackingNumber) {
        return parcelRepository.findByTrackingNumber(trackingNumber).orElseThrow(
                () -> new NotFoundException("Parcel with tracking number " + trackingNumber + " not found"));
    }

    @Override
    public Page<Parcel> findAll(ParcelSearchParams params, Pageable pageable) {
        return parcelRepository.findAll(buildSearchFilter(params), pageable);
    }

    @Override
    public ParcelStatistic buildStatistic(ParcelSearchParams params) {
        List<Parcel> parcels = parcelRepository.findAll(buildSearchFilter(params));

        // Start every status and delivery type at 0, so missing ones still appear in the result.
        Map<ParcelStatus, Long> countByStatus = new HashMap<>();
        for (ParcelStatus status : ParcelStatus.values()) {
            countByStatus.put(status, 0L);
        }
        Map<DeliveryType, Long> countByDeliveryType = new HashMap<>();
        for (DeliveryType deliveryType : DeliveryType.values()) {
            countByDeliveryType.put(deliveryType, 0L);
        }

        double totalWeight = 0;
        double totalPrice = 0;
        Parcel mostExpensive = null;
        Parcel cheapest = null;
        Parcel heaviest = null;
        Parcel lightest = null;

        for (Parcel parcel : parcels) {
            totalWeight += parcel.getWeight();
            totalPrice += parcel.getPrice();

            countByStatus.put(parcel.getStatus(), countByStatus.get(parcel.getStatus()) + 1);
            countByDeliveryType.put(parcel.getDeliveryType(), countByDeliveryType.get(parcel.getDeliveryType()) + 1);

            if (mostExpensive == null || parcel.getPrice() > mostExpensive.getPrice()) {
                mostExpensive = parcel;
            }
            if (cheapest == null || parcel.getPrice() < cheapest.getPrice()) {
                cheapest = parcel;
            }
            if (heaviest == null || parcel.getWeight() > heaviest.getWeight()) {
                heaviest = parcel;
            }
            if (lightest == null || parcel.getWeight() < lightest.getWeight()) {
                lightest = parcel;
            }
        }

        // Avoid dividing by zero when no parcels match.
        double averageWeight = parcels.isEmpty() ? 0 : totalWeight / parcels.size();
        double averagePrice = parcels.isEmpty() ? 0 : totalPrice / parcels.size();

        ParcelStatistic statistic = new ParcelStatistic();
        statistic.setTotalParcels(parcels.size());
        statistic.setAverageWeight(averageWeight);
        statistic.setAveragePrice(averagePrice);
        statistic.setParcelsCountByStatus(countByStatus);
        statistic.setParcelsCountByDeliveryType(countByDeliveryType);
        statistic.setMostExpensiveParcel(mostExpensive);
        statistic.setCheapestParcel(cheapest);
        statistic.setHeaviestParcel(heaviest);
        statistic.setLightestParcel(lightest);
        return statistic;
    }

    @Override
    @Transactional
    public Parcel create(CreateParcelRequest request) {
        Parcel parcel = new Parcel();
        parcel.setTrackingNumber(generator.uuid());
        parcel.setSender(request.getSender());
        parcel.setRecipient(request.getRecipient());
        parcel.setWeight(request.getWeight());
        parcel.setStatus(ParcelStatus.CREATED);
        parcel.setDeliveryType(request.getDeliveryType());
        parcel.setPrice(calculatePrice(request));
        return parcelRepository.save(parcel);
    }

    @Override
    @Transactional
    public Parcel updateStatus(String trackingNumber, ParcelStatus status) {
        Parcel parcel = getByTrackingNumber(trackingNumber);
        parcel.setStatus(status);
        return parcelRepository.save(parcel);
    }

    // Still called by ComponentForProduceCycleDependency, so it has to stay.
    public void methodForTrick() {
    }

    // Each helper returns "no filter" when its value is null or empty, so that field matches anything.
    private Specification<Parcel> buildSearchFilter(ParcelSearchParams params) {
        return Specification.<Parcel>where(equal(Parcel.Fields.trackingNumber, params.getTrackingNumber()))
                .and(iLike(Parcel.Fields.sender, params.getSender()))
                .and(iLike(Parcel.Fields.recipient, params.getRecipient()))
                .and(gte(Parcel.Fields.weight, params.getFromWeight()))
                .and(lte(Parcel.Fields.weight, params.getToWeight()))
                .and(gte(Parcel.Fields.price, params.getFromPrice()))
                .and(lte(Parcel.Fields.price, params.getToPrice()))
                .and(in(Parcel.Fields.status, params.getStatuses()))
                .and(in(Parcel.Fields.deliveryType, params.getDeliveryTypes()));
    }

    // Exact match (SpecificationUtils has no "equals" helper).
    private Specification<Parcel> equal(String field, Object value) {
        if (value == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private double calculatePrice(CreateParcelRequest request) {
        for (PriceCalculator calculator : priceCalculators) {
            if (calculator.getDeliveryType() == request.getDeliveryType()) {
                return calculator.calculatePrice(request);
            }
        }
        throw new IllegalArgumentException("No price calculator for delivery type " + request.getDeliveryType());
    }
}
