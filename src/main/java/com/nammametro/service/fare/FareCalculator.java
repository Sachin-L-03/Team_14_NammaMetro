package com.nammametro.service.fare;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ============================================================
 *  SOLID Principle: Open/Closed Principle (OCP)
 * ============================================================
 *
 *  FareCalculator is CLOSED for modification — it iterates over a
 *  list of FareRule implementations and applies them in order.
 *
 *  To add a new fare rule (e.g., SeniorCitizenDiscountRule, PeakHourSurcharge),
 *  simply create a new class implementing FareRule and register it in the
 *  constructor — zero changes to the calculation logic.
 *
 *  SRP: This class has one responsibility — orchestrating fare rules to
 *       produce a fare calculation result.
 * ============================================================
 */
@Service
public class FareCalculator {

    private final List<FareRule> rules;

    /**
     * Registers all fare rules. To add a new rule, simply add it here.
     * OCP: existing rules remain untouched.
     */
    public FareCalculator() {
        this.rules = new ArrayList<>();
        this.rules.add(new BaseFareRule());
        this.rules.add(new MetroCardDiscountRule());
        // OCP: Add new rules here without modifying existing ones:
        // this.rules.add(new SeniorCitizenDiscountRule());
        // this.rules.add(new PeakHourSurchargeRule());

        // Sort by execution order
        this.rules.sort(Comparator.comparingInt(FareRule::getOrder));
    }

    /**
     * Calculates the fare for a journey.
     *
     * @param distanceKm   total journey distance in kilometres
     * @param hasMetroCard whether the passenger has a metro card
     * @return detailed breakdown of the fare
     */
    public FareBreakdown calculate(BigDecimal distanceKm, boolean hasMetroCard) {
        FareBreakdown breakdown = new FareBreakdown();
        breakdown.setDistanceKm(distanceKm);
        breakdown.setMetroCardApplied(hasMetroCard);

        // Calculate base fare (₹10 for first 2 km)
        BigDecimal baseFare = new BigDecimal("10");
        breakdown.setBaseFare(baseFare);

        // Calculate per-km charge
        BigDecimal baseKm = new BigDecimal("2");
        BigDecimal perKmCharge = BigDecimal.ZERO;
        if (distanceKm.compareTo(baseKm) > 0) {
            perKmCharge = distanceKm.subtract(baseKm)
                    .multiply(new BigDecimal("5"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        breakdown.setPerKmCharge(perKmCharge);

        // Apply all rules in order
        BigDecimal currentFare = BigDecimal.ZERO;
        for (FareRule rule : rules) {
            currentFare = rule.apply(currentFare, distanceKm, hasMetroCard);
            breakdown.addRuleDescription(rule.getDescription());
        }

        // Calculate discount amount
        BigDecimal beforeDiscount = baseFare.add(perKmCharge);
        BigDecimal discountAmt = beforeDiscount.subtract(currentFare).max(BigDecimal.ZERO);
        breakdown.setDiscountAmount(discountAmt);

        // Round up to nearest ₹5
        BigDecimal finalFare = roundUpToNearest5(currentFare);
        breakdown.setFinalFare(finalFare);

        return breakdown;
    }

    /**
     * Rounds a fare up to the nearest ₹5.
     * Examples: ₹13 → ₹15, ₹20 → ₹20, ₹21 → ₹25
     */
    private BigDecimal roundUpToNearest5(BigDecimal fare) {
        BigDecimal five = new BigDecimal("5");
        BigDecimal divided = fare.divide(five, 0, RoundingMode.CEILING);
        return divided.multiply(five);
    }
}
