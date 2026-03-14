package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;

/**
 * Strategy for normal/standard items.
 * Quality decreases by 1 per day, by 2 after sell-in date.
 *
 * This is the default strategy for items that don't match any specific type.
 *
 * Demonstrates:
 * - Strategy Pattern: Encapsulates normal item behavior
 * - Single Responsibility: Only handles normal item logic
 * - Inheritance: Reuses template method from base class
 */
public class NormalItemUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int NORMAL_DEGRADATION = 1;

    public NormalItemUpdateStrategy(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        super(qualityAdjuster, sellInAdjuster);
    }

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        qualityAdjuster.decreaseQuality(item, NORMAL_DEGRADATION);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        if (sellInAdjuster.isExpired(item)) {
            // Quality degrades twice as fast after sell-in date
            qualityAdjuster.decreaseQuality(item, NORMAL_DEGRADATION);
        }
    }

    @Override
    public boolean canHandle(String itemName) {
        // Default strategy - handles all items not handled by specific strategies
        return true;
    }
}
