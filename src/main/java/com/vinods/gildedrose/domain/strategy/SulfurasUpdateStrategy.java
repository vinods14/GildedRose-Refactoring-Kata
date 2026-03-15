package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

import static com.vinods.gildedrose.constants.ItemNames.SULFURAS;

/**
 * Strategy for Sulfuras - the legendary item.
 * Quality and sell-in never change for legendary items.
 */
public class SulfurasUpdateStrategy extends BaseQualityUpdateStrategy {

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
