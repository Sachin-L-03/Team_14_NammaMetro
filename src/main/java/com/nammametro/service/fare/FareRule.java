package com.nammametro.service.fare;

import java.math.BigDecimal;

/**
 * ============================================================
 *  SOLID Principle: Open/Closed Principle (OCP)
 * ============================================================
 *
 *  FareRule is the extension point for the fare calculation system.
 *  To add a new fare rule (e.g., senior citizen discount, peak-hour
 *  surcharge, student discount), simply create a new class that
 *  implements FareRule — no modification to FareCalculator needed.
 *
 *  This makes the fare engine OPEN for extension but CLOSED for
 *  modification.
 *
 *  SRP: This interface has one responsibility — defining the contract
 *       for a single fare calculation rule.
 * ============================================================
 */
public interface FareRule {

    /**
     * Applies this rule to the current fare.
     *
     * @param currentFare  the fare after previous rules have been applied
     * @param distanceKm   total journey distance in kilometres
     * @param hasMetroCard whether the passenger has a metro card
     * @return the fare after this rule is applied
     */
    BigDecimal apply(BigDecimal currentFare, BigDecimal distanceKm, boolean hasMetroCard);

    /**
     * A human-readable description for the fare breakdown.
     */
    String getDescription();

    /**
     * The order in which this rule should be applied (lower = earlier).
     */
    int getOrder();
}
