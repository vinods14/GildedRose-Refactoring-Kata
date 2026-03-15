package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

/**
 * Strategy for normal/standard items.
 * Quality decreases by 1 per day, by 2 after sell-in date.
 *
 * This is the default strategy for items that don't match any specific type.
 */
public class NormalItemUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int NORMAL_DEGRADATION = 1;

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        decreaseQuality(item, NORMAL_DEGRADATION);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        // Quality degrades twice as fast after sell-in date
        decreaseQuality(item, NORMAL_DEGRADATION);
    }

    @Override
    public boolean canHandle(String itemName) {
        // Default strategy - handles all items not handled by specific strategies
        return true;
    }
}
