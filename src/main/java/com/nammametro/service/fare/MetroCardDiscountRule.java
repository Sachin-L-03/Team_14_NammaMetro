package com.nammametro.service.fare;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Metro card discount rule: 10% off for passengers with a metro card.
 *
 * OCP: This is one concrete fare rule. It can be swapped, extended, or
 *      supplemented with additional rules without modifying FareCalculator.
 *
 * SRP: This class has one responsibility — applying the metro card discount.
 */
public class MetroCardDiscountRule implements FareRule {

    private static final BigDecimal DISCOUNT_PERCENT = new BigDecimal("0.10");

    @Override
    public BigDecimal apply(BigDecimal currentFare, BigDecimal distanceKm, boolean hasMetroCard) {
        if (!hasMetroCard) {
            return currentFare; // no discount
        }
        BigDecimal discount = currentFare.multiply(DISCOUNT_PERCENT).setScale(2, RoundingMode.HALF_UP);
        return currentFare.subtract(discount);
    }

    @Override
    public String getDescription() {
        return "Metro Card Discount: 10% off";
    }

    @Override
    public int getOrder() {
        return 10; // applied after base fare
    }
}
