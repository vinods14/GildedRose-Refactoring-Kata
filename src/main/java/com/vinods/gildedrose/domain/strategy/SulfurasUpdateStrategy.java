package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;

import static com.vinods.gildedrose.constants.ItemNames.SULFURAS;

/**
 * Strategy for Sulfuras - the legendary item.
 * Quality and sell-in never change for legendary items.
 *
 * Demonstrates:
 * - Liskov Substitution Principle: Can replace any ItemUpdateStrategy
 * - Open/Closed Principle: Extends base without modifying it
 * - Strategy Pattern: Encapsulates Sulfuras-specific behavior
 */
public class SulfurasUpdateStrategy extends BaseQualityUpdateStrategy {

    public SulfurasUpdateStrategy(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        super(qualityAdjuster, sellInAdjuster);
    }

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        // Legendary item - quality never changes
    }

    @Override
    protected void decrementSellIn(Item item) {
        // Legendary item - sell-in never changes
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        // Legendary item - quality never changes
    }

    @Override
    public boolean canHandle(String itemName) {
        return SULFURAS.equals(itemName);
    }
}
