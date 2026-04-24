package com.nammametro.service.fare;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Base fare rule: ₹10 for the first 2 km, then ₹5 per km.
 *
 * OCP: This is one concrete fare rule. Adding a new rule (e.g.,
 *      SeniorCitizenDiscountRule) does not require modifying this class.
 *
 * SRP: This class has one responsibility — calculating the base distance-based fare.
 */
public class BaseFareRule implements FareRule {

    private static final BigDecimal BASE_FARE = new BigDecimal("10");
    private static final BigDecimal BASE_KM = new BigDecimal("2");
    private static final BigDecimal PER_KM_RATE = new BigDecimal("5");

    @Override
    public BigDecimal apply(BigDecimal currentFare, BigDecimal distanceKm, boolean hasMetroCard) {
        if (distanceKm.compareTo(BASE_KM) <= 0) {
            return BASE_FARE;
        }
        BigDecimal extraKm = distanceKm.subtract(BASE_KM);
        BigDecimal extraFare = extraKm.multiply(PER_KM_RATE).setScale(2, RoundingMode.HALF_UP);
        return BASE_FARE.add(extraFare);
    }

    @Override
    public String getDescription() {
        return "Base fare: ₹10 (first 2 km) + ₹5/km";
    }

    @Override
    public int getOrder() {
        return 1; // applied first
    }
}
