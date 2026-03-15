package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

import static com.vinods.gildedrose.constants.ItemNames.CONJURED_PREFIX;

/**
 * Strategy for Conjured items.
 * Quality degrades twice as fast as normal items:
 * - Before sell-in: decreases by 2 per day
 * - After sell-in: decreases by 4 per day
 */
public class ConjuredItemUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int CONJURED_DEGRADATION = 2; // 2x normal degradation

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        decreaseQuality(item, CONJURED_DEGRADATION);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        // Degrades twice as fast after sell-in date
        decreaseQuality(item, CONJURED_DEGRADATION);
    }

    @Override
    public boolean canHandle(String itemName) {
        return itemName != null && itemName.startsWith(CONJURED_PREFIX);
    }
}
