package com.nammametro.service.fare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO that holds the breakdown of a fare calculation.
 * Contains each applied rule's description and the final fare.
 */
public class FareBreakdown {

    private BigDecimal baseFare;
    private BigDecimal perKmCharge;
    private BigDecimal discountAmount;
    private BigDecimal finalFare;
    private BigDecimal distanceKm;
    private boolean metroCardApplied;
    private final List<String> ruleDescriptions = new ArrayList<>();

    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }

    public BigDecimal getPerKmCharge() { return perKmCharge; }
    public void setPerKmCharge(BigDecimal perKmCharge) { this.perKmCharge = perKmCharge; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalFare() { return finalFare; }
    public void setFinalFare(BigDecimal finalFare) { this.finalFare = finalFare; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

    public boolean isMetroCardApplied() { return metroCardApplied; }
    public void setMetroCardApplied(boolean metroCardApplied) { this.metroCardApplied = metroCardApplied; }

    public List<String> getRuleDescriptions() { return ruleDescriptions; }
    public void addRuleDescription(String desc) { this.ruleDescriptions.add(desc); }
}
