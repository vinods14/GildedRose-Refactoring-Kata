package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

import static com.vinods.gildedrose.constants.ItemNames.AGED_BRIE;

/**
 * Strategy for Aged Brie.
 * Quality increases by 1 per day, by 2 after sell-in date (max 50).
 */
public class AgedBrieUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int APPRECIATION_RATE = 1;

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        increaseQuality(item, APPRECIATION_RATE);
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        // Quality increases twice as fast after sell-in date
        increaseQuality(item, APPRECIATION_RATE);
    }

    @Override
    public boolean canHandle(String itemName) {
        return AGED_BRIE.equals(itemName);
    }
}
