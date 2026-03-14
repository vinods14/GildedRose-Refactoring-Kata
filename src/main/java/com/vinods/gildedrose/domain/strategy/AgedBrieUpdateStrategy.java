package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;

import static com.vinods.gildedrose.constants.ItemNames.AGED_BRIE;

/**
 * Strategy for Aged Brie.
 * Quality increases by 1 per day, by 2 after sell-in date (max 50).
 *
 * Aged Brie is unique because it appreciates in value over time,
 * unlike normal items that degrade.
 *
 * Demonstrates:
 * - Strategy Pattern: Different behavior for different item types
 * - Polymorphism: Same interface, different implementation
 * - Encapsulation: Aged Brie logic isolated from other items
 */
public class AgedBrieUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int APPRECIATION_RATE = 1;

    public AgedBrieUpdateStrategy(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        super(qualityAdjuster, sellInAdjuster);
    }

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        qualityAdjuster.increaseQuality(item, APPRECIATION_RATE);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        if (sellInAdjuster.isExpired(item)) {
            // Quality increases twice as fast after sell-in date
            qualityAdjuster.increaseQuality(item, APPRECIATION_RATE);
        }
    }

    @Override
    public boolean canHandle(String itemName) {
        return AGED_BRIE.equals(itemName);
    }
}
