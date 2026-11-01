package com.example.demo.service;

import com.example.demo.dto.CreateParcelRequest;
import com.example.demo.dto.ParcelSearchParams;
import com.example.demo.dto.ParcelStatistic;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.DeliveryType;
import com.example.demo.model.Parcel;
import com.example.demo.model.ParcelStatus;
import com.example.demo.repository.ParcelRepository;
import com.example.demo.util.BasePgSqlIT;
import com.example.demo.util.CommonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.example.demo.service.ParcelServiceTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "spring.jpa.show-sql=true")
class ParcelServiceTest implements BasePgSqlIT {

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private ParcelService parcelService;

    @MockitoBean
    private CommonGenerator generator;

    @BeforeEach
    void setUp() {
        parcelRepository.deleteAll();
    }

    @Test
    void givenParcelByTrackingNumber_getByTrackingNumber_shouldReturnParcel() {
        parcelRepository.save(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS());

        Parcel parcel = parcelService.getByTrackingNumber(TRACKING_NUMBER_1);

        assertThat(parcel)
                .usingRecursiveComparison()
                .ignoringFields(Parcel.Fields.id)
                .isEqualTo(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS());
    }

    @Test
    void givenNoParcels_getByTrackingNumber_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> parcelService.getByTrackingNumber("invalidTrackingNumber"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel with tracking number invalidTrackingNumber not found");
    }

    @ParameterizedTest
    @MethodSource("provideFindAllTestArguments")
    void givenParcelsInDb_findAll_shouldFindUsingSearchParams(ParcelSearchParams searchParams,
                                                              List<Parcel> matchingParcels) {
        parcelRepository.saveAll(buildInitialParcels());

        Page<Parcel> parcels = parcelService.findAll(searchParams, PAGEABLE);

        assertThat(parcels)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(Parcel.Fields.id)
                .containsExactlyInAnyOrderElementsOf(matchingParcels);
    }

    @ParameterizedTest
    @MethodSource("provideBuildStatisticTestArguments")
    void givenParcelsInDb_buildStatistic_shouldBuildStatisticUsingSearchParams(ParcelSearchParams searchParams,
                                                                               ParcelStatistic expectedStatistic) {
        parcelRepository.saveAll(buildInitialParcels());

        ParcelStatistic statistic = parcelService.buildStatistic(searchParams);

        assertThat(statistic)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*." + Parcel.Fields.id)
                .isEqualTo(expectedStatistic);
    }

    @ParameterizedTest
    @MethodSource("provideCreateTestArguments")
    void givenCreateParcelRequest_create_shouldSaveToDb(DeliveryType deliveryType, Double price) {
        when(generator.uuid()).thenReturn(CREATED_PARCEL_TRACKING_NUMBER);

        Parcel createdParcel = parcelService.create(buildCreateParcelRequest(deliveryType));

        Parcel parcelFromDb = parcelRepository.findByTrackingNumber(createdParcel.getTrackingNumber()).orElseThrow();

        assertThat(createdParcel).isEqualTo(parcelFromDb);
        assertThat(createdParcel)
                .usingRecursiveComparison()
                .ignoringFields(Parcel.Fields.id)
                .isEqualTo(buildCreatedParcel(deliveryType, price));
    }

    @Test
    void givenParcelInDb_updateStatus_shouldUpdateParcelStatus() {
        parcelRepository.save(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS());

        Parcel updatedParcel = parcelService.updateStatus(TRACKING_NUMBER_1, ParcelStatus.IN_TRANSIT);

        assertThat(updatedParcel)
                .usingRecursiveComparison()
                .ignoringFields(Parcel.Fields.id)
                .isEqualTo(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS().toBuilder()
                        .status(ParcelStatus.IN_TRANSIT)
                        .build());
    }

    @Test
    void givenNoParcelInDb_updateStatus_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> parcelService.updateStatus("invalidTrackingNumber", ParcelStatus.IN_TRANSIT))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Parcel with tracking number invalidTrackingNumber not found");
    }

    static Stream<Arguments> provideFindAllTestArguments() {
        return Stream.of(
                Arguments.of(
                        ParcelSearchParams.builder().build(),
                        buildInitialParcels()
                ),
                Arguments.of(
                        ParcelSearchParams.builder().trackingNumber(TRACKING_NUMBER_1).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().sender(SENDER_ANTONY).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().recipient(RECIPIENT_JOHN).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().fromWeight(FROM_WEIGHT_MATCHING_1_2_4).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().toWeight(TO_WEIGHT_MATCHING_2_3_5).build(),
                        List.of(buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM(),
                                buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .fromWeight(FROM_WEIGHT_MATCHING_1_2_4).toWeight(TO_WEIGHT_MATCHING_2_3_5).build(),
                        List.of(buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().fromPrice(FROM_PRICE_MATCHING_1_2_4).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                ),
                Arguments.of(
                        ParcelSearchParams.builder().toPrice(TO_PRICE_MATCHING_2_3_5).build(),
                        List.of(buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM(),
                                buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .fromPrice(FROM_PRICE_MATCHING_1_2_4).toPrice(TO_PRICE_MATCHING_2_3_5).build(),
                        List.of(buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .statuses(List.of(ParcelStatus.CREATED, ParcelStatus.IN_TRANSIT)).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .deliveryTypes(List.of(DeliveryType.EXPRESS, DeliveryType.DEFAULT)).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                                buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS(),
                                buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .sender(SENDER_ANTONY)
                                .fromWeight(FROM_WEIGHT_MATCHING_1_2_4)
                                .fromPrice(FROM_PRICE_MATCHING_1_2_4)
                                .statuses(List.of(ParcelStatus.CREATED, ParcelStatus.DELIVERED))
                                .deliveryTypes(List.of(DeliveryType.EXPRESS, DeliveryType.DEFAULT))
                                .build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .sender(SENDER_LUCAS)
                                .recipient(RECIPIENT_OLIVIA)
                                .statuses(List.of(ParcelStatus.DELIVERED))
                                .build(),
                        List.of(buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .sender(SENDER_LUCAS)
                                .recipient(RECIPIENT_OLIVIA)
                                .build(),
                        List.of(buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM(),
                                buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .recipient(RECIPIENT_JOHN)
                                .deliveryTypes(List.of(DeliveryType.EXPRESS, DeliveryType.DEFAULT)).build(),
                        List.of(buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                                buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT())
                )
        );
    }

    static Stream<Arguments> provideBuildStatisticTestArguments() {
        return Stream.of(
                Arguments.of(
                        ParcelSearchParams.builder().build(),
                        buildParcelStatistic_Initial()
                ),
                Arguments.of(
                        ParcelSearchParams.builder()
                                .deliveryTypes(List.of(DeliveryType.EXPRESS, DeliveryType.DEFAULT)).build(),
                        buildParcelStatistic_1_2_4_5()
                ),
                Arguments.of(
                        ParcelSearchParams.builder().trackingNumber("invalidTrackingNumber").build(),
                        buildParcelStatistic_Empty()
                )
        );
    }

    static Stream<Arguments> provideCreateTestArguments() {
        return Stream.of(
                Arguments.of(DeliveryType.EXPRESS, 603.0),
                Arguments.of(DeliveryType.DEFAULT, 402.0),
                Arguments.of(DeliveryType.ECONOM, 201.0)
        );
    }

    public static class TestResources {

        public static final String SENDER_ANTONY = "Antony Williams";
        public static final String SENDER_EMMA = "Emma Brown";
        public static final String SENDER_LUCAS = "Lucas Johnson";

        public static final String RECIPIENT_JOHN = "John Doe";
        public static final String RECIPIENT_OLIVIA = "Olivia Davis";
        public static final String RECIPIENT_JAMES = "James Taylor";

        public static final String TRACKING_NUMBER_1 = "fa635f04-3094-46b2-b6f7-8fbe377c8be3";
        public static final Double WEIGHT_1 = 50.0;
        public static final Double PRICE_1 = 70.0;
        public static final ParcelStatus STATUS_1 = ParcelStatus.CREATED;
        public static final DeliveryType DELIVERY_TYPE_1 = DeliveryType.EXPRESS;

        public static final String TRACKING_NUMBER_2 = "a12c98d1-7b56-44a3-bb57-928cc501e2af";
        public static final Double WEIGHT_2 = 30.5;
        public static final Double PRICE_2 = 45.0;
        public static final ParcelStatus STATUS_2 = ParcelStatus.IN_TRANSIT;
        public static final DeliveryType DELIVERY_TYPE_2 = DeliveryType.DEFAULT;

        public static final String TRACKING_NUMBER_3 = "d67237c5-8ec3-47b3-9b14-3d94a3ed37d2";
        public static final Double WEIGHT_3 = 10.0;
        public static final Double PRICE_3 = 20.0;
        public static final ParcelStatus STATUS_3 = ParcelStatus.DELIVERED;
        public static final DeliveryType DELIVERY_TYPE_3 = DeliveryType.ECONOM;

        public static final String TRACKING_NUMBER_4 = "b1f248f3-4c67-4d27-b4e9-bc6522c4d623";
        public static final Double WEIGHT_4 = 75.0;
        public static final Double PRICE_4 = 90.0;
        public static final ParcelStatus STATUS_4 = ParcelStatus.DELIVERED;
        public static final DeliveryType DELIVERY_TYPE_4 = DeliveryType.EXPRESS;

        public static final String TRACKING_NUMBER_5 = "c45a821a-bda1-4e12-b7d6-2e6a5ef62c14";
        public static final Double WEIGHT_5 = 22.0;
        public static final Double PRICE_5 = 35.0;
        public static final ParcelStatus STATUS_5 = ParcelStatus.IN_TRANSIT;
        public static final DeliveryType DELIVERY_TYPE_5 = DeliveryType.DEFAULT;

        public static final String CREATE_PARCEL_REQUEST_SENDER = "William";
        public static final String CREATE_PARCEL_REQUEST_RECIPIENT = "Arthur";
        public static final Double CREATE_PARCEL_REQUEST_WEIGHT = 10.0;
        public static final String CREATED_PARCEL_TRACKING_NUMBER = "ab0ee696-c5c6-4e64-b278-5ead7fc78ca8";

        public static final Double FROM_WEIGHT_MATCHING_1_2_4 = 30.0;
        public static final Double TO_WEIGHT_MATCHING_2_3_5 = 40.0;

        public static final Double FROM_PRICE_MATCHING_1_2_4 = 45.0;
        public static final Double TO_PRICE_MATCHING_2_3_5 = 45.0;

        public static final Integer PAGE_NUMBER = 0;
        public static final Integer PAGE_SIZE = 10;
        public static final Pageable PAGEABLE = PageRequest.of(PAGE_NUMBER, PAGE_SIZE);

        public static CreateParcelRequest buildCreateParcelRequest(DeliveryType deliveryType) {
            return CreateParcelRequest.builder()
                    .sender(CREATE_PARCEL_REQUEST_SENDER)
                    .recipient(CREATE_PARCEL_REQUEST_RECIPIENT)
                    .weight(CREATE_PARCEL_REQUEST_WEIGHT)
                    .deliveryType(deliveryType)
                    .build();
        }

        public static Parcel buildCreatedParcel(DeliveryType deliveryType, Double price) {
            return Parcel.builder()
                    .trackingNumber(CREATED_PARCEL_TRACKING_NUMBER)
                    .sender(CREATE_PARCEL_REQUEST_SENDER)
                    .recipient(CREATE_PARCEL_REQUEST_RECIPIENT)
                    .weight(CREATE_PARCEL_REQUEST_WEIGHT)
                    .price(price)
                    .status(ParcelStatus.CREATED)
                    .deliveryType(deliveryType)
                    .build();
        }

        public static List<Parcel> buildInitialParcels() {
            return List.of(
                    buildParcel_1_from_Antony_to_John_CREATED_EXPRESS(),
                    buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT(),
                    buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM(),
                    buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS(),
                    buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT()
            );
        }

        public static Parcel buildParcel_1_from_Antony_to_John_CREATED_EXPRESS() {
            return Parcel.builder()
                    .trackingNumber(TRACKING_NUMBER_1)
                    .sender(SENDER_ANTONY)
                    .recipient(RECIPIENT_JOHN)
                    .weight(WEIGHT_1)
                    .price(PRICE_1)
                    .status(STATUS_1)
                    .deliveryType(DELIVERY_TYPE_1)
                    .build();
        }

        public static Parcel buildParcel_2_from_Emma_to_John_IN_TRANSIT_DEFAULT() {
            return Parcel.builder()
                    .trackingNumber(TRACKING_NUMBER_2)
                    .sender(SENDER_EMMA)
                    .recipient(RECIPIENT_JOHN)
                    .weight(WEIGHT_2)
                    .price(PRICE_2)
                    .status(STATUS_2)
                    .deliveryType(DELIVERY_TYPE_2)
                    .build();
        }

        public static Parcel buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM() {
            return Parcel.builder()
                    .trackingNumber(TRACKING_NUMBER_3)
                    .sender(SENDER_LUCAS)
                    .recipient(RECIPIENT_OLIVIA)
                    .weight(WEIGHT_3)
                    .price(PRICE_3)
                    .status(STATUS_3)
                    .deliveryType(DELIVERY_TYPE_3)
                    .build();
        }

        public static Parcel buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS() {
            return Parcel.builder()
                    .trackingNumber(TRACKING_NUMBER_4)
                    .sender(SENDER_ANTONY)
                    .recipient(RECIPIENT_JAMES)
                    .weight(WEIGHT_4)
                    .price(PRICE_4)
                    .status(STATUS_4)
                    .deliveryType(DELIVERY_TYPE_4)
                    .build();
        }

        public static Parcel buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT() {
            return Parcel.builder()
                    .trackingNumber(TRACKING_NUMBER_5)
                    .sender(SENDER_LUCAS)
                    .recipient(RECIPIENT_OLIVIA)
                    .weight(WEIGHT_5)
                    .price(PRICE_5)
                    .status(STATUS_5)
                    .deliveryType(DELIVERY_TYPE_5)
                    .build();
        }

        public static ParcelStatistic buildParcelStatistic_Initial() {
            return ParcelStatistic.builder()
                    .totalParcels(5)
                    .averageWeight((WEIGHT_1 + WEIGHT_2 + WEIGHT_3 + WEIGHT_4 + WEIGHT_5) / 5)
                    .averagePrice((PRICE_1 + PRICE_2 + PRICE_3 + PRICE_4 + PRICE_5) / 5)
                    .parcelsCountByStatus(Map.of(
                            ParcelStatus.CREATED, 1L,
                            ParcelStatus.IN_TRANSIT, 2L,
                            ParcelStatus.DELIVERED, 2L
                    ))
                    .parcelsCountByDeliveryType(Map.of(
                            DeliveryType.ECONOM, 1L,
                            DeliveryType.DEFAULT, 2L,
                            DeliveryType.EXPRESS, 2L
                    ))
                    .mostExpensiveParcel(buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                    .cheapestParcel(buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM())
                    .heaviestParcel(buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                    .lightestParcel(buildParcel_3_from_Lucas_to_Olivia_DELIVERED_ECONOM())
                    .build();
        }

        public static ParcelStatistic buildParcelStatistic_1_2_4_5() {
            return ParcelStatistic.builder()
                    .totalParcels(4)
                    .averageWeight((WEIGHT_1 + WEIGHT_2 + WEIGHT_4 + WEIGHT_5) / 4)
                    .averagePrice((PRICE_1 + PRICE_2 + PRICE_4 + PRICE_5) / 4)
                    .parcelsCountByStatus(Map.of(
                            ParcelStatus.CREATED, 1L,
                            ParcelStatus.IN_TRANSIT, 2L,
                            ParcelStatus.DELIVERED, 1L
                    ))
                    .parcelsCountByDeliveryType(Map.of(
                            DeliveryType.ECONOM, 0L,
                            DeliveryType.DEFAULT, 2L,
                            DeliveryType.EXPRESS, 2L
                    ))
                    .mostExpensiveParcel(buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                    .cheapestParcel(buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                    .heaviestParcel(buildParcel_4_from_Antony_to_James_DELIVERED_EXPRESS())
                    .lightestParcel(buildParcel_5_from_Lucas_to_Olivia_IN_TRANSIT_DEFAULT())
                    .build();
        }

        public static ParcelStatistic buildParcelStatistic_Empty() {
            return ParcelStatistic.builder()
                    .totalParcels(0)
                    .averageWeight(0)
                    .averagePrice(0)
                    .parcelsCountByStatus(Map.of(
                            ParcelStatus.CREATED, 0L,
                            ParcelStatus.IN_TRANSIT, 0L,
                            ParcelStatus.DELIVERED, 0L
                    ))
                    .parcelsCountByDeliveryType(Map.of(
                            DeliveryType.ECONOM, 0L,
                            DeliveryType.DEFAULT, 0L,
                            DeliveryType.EXPRESS, 0L
                    ))
                    .mostExpensiveParcel(null)
                    .cheapestParcel(null)
                    .heaviestParcel(null)
                    .lightestParcel(null)
                    .build();
        }
    }
}