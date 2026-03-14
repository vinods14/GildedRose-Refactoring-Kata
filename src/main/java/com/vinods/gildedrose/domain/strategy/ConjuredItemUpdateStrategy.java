package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;

import static com.vinods.gildedrose.constants.ItemNames.CONJURED_PREFIX;

/**
 * Strategy for Conjured items.
 * Quality degrades twice as fast as normal items:
 * - Before sell-in: decreases by 2 per day
 * - After sell-in: decreases by 4 per day
 *
 * This strategy demonstrates extensibility - adding a new item type
 * requires only creating this class and registering it in the factory.
 * No modifications to existing code needed (Open/Closed Principle).
 *
 * Demonstrates:
 * - Open/Closed Principle: Extension without modification
 * - Strategy Pattern: New behavior without changing existing strategies
 * - Liskov Substitution: Can replace any ItemUpdateStrategy
 */
public class ConjuredItemUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int CONJURED_DEGRADATION = 2; // 2x normal degradation

    public ConjuredItemUpdateStrategy(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        super(qualityAdjuster, sellInAdjuster);
    }

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        qualityAdjuster.decreaseQuality(item, CONJURED_DEGRADATION);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        if (sellInAdjuster.isExpired(item)) {
            // Degrades twice as fast after sell-in date
            qualityAdjuster.decreaseQuality(item, CONJURED_DEGRADATION);
        }
    }

    @Override
    public boolean canHandle(String itemName) {
        return itemName != null && itemName.startsWith(CONJURED_PREFIX);
    }
}
