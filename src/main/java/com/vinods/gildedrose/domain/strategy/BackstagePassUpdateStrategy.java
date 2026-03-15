package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

import static com.vinods.gildedrose.constants.ItemNames.BACKSTAGE_PASS;
import static com.vinods.gildedrose.constants.QualityConstants.*;

/**
 * Strategy for Backstage Passes.
 * Complex quality rules based on days until concert:
 * - More than 10 days: quality increases by 1
 * - 6-10 days: quality increases by 2
 * - 1-5 days: quality increases by 3
 * - After concert (expired): quality drops to 0
 */
public class BackstagePassUpdateStrategy extends BaseQualityUpdateStrategy {

    private static final int BASE_INCREASE = 1;
    private static final int BONUS_INCREASE_10_DAYS = 1;
    private static final int BONUS_INCREASE_5_DAYS = 1; // Additional bonus on top of 10-day bonus

    @Override
    protected void updateQualityBeforeSellIn(Item item) {
        // Base increase - always happens
        increaseQuality(item, BASE_INCREASE);

        // Bonus increase when less than 11 days (10 days or less)
        if (item.sellIn < BACKSTAGE_FIRST_THRESHOLD) {
            increaseQuality(item, BONUS_INCREASE_10_DAYS);
        }

        // Additional bonus when less than 6 days (5 days or less)
        if (item.sellIn < BACKSTAGE_SECOND_THRESHOLD) {
            increaseQuality(item, BONUS_INCREASE_5_DAYS);
        }
    }

    @Override
    protected void updateQualityAfterSellIn(Item item) {
        // Concert has passed - passes are worthless
        setQuality(item, MIN_QUALITY);
    }

    @Override
    public boolean canHandle(String itemName) {
        return BACKSTAGE_PASS.equals(itemName);
    }
}
