# TEST TASK

## INTRO

Here is a service for parcel management.
The service performs such tasks:
- Creation of parcel
- Finding parcel by track number
- Finding parcels by filter
- Building parcels statistic
- Update parcel status

## TASK

- Start the docker environment.
- Start the application (fix start errors).
- Refactor `ParcelServiceImpl`.
  You can do any changes you want in the service but not change the contract with interface
  (amount of PriceCalculator's can be extended in the future).
- Implement `findAll` method in `ParcelServiceImpl`.

  Note:
    - If field is null in the filters, so any value is valid.
      - Example: `trackingNumber == null`, so find parcels with any `trackingNumber`.
    - If empty or null list is in the filter, so any value is valid.
      - Example 1: `statuses == [] || statuses == null`, so find parcels with any `status`.
      - Example 2: `statuses == [CREATED, DELIVERED]`, so find parcels where `status` is `CREATED` or is `DELIVERED`.
    - For numbers comparisons use non-strict inequality `(<=, >=)`.

- Implement `buildStatistic` method in `ParcelServiceImpl`.

  Note:
    - If parcels with some `Status` or `DeliveryType` is absent so `parcelsCountByStatus` or `parcelsCountByDeliveryType`
      must contain map entry with 0 (Don't just skip this case).
      - Example: no parcels with `status == EXPRESS` so `parcelsCountByStatus` must contain map entry `{EXPRESS=0}`.

- To check the correctness of the solution, run the tests.
- Send single `ParcelServiceImpl.java` file to HR.
- Good luck!