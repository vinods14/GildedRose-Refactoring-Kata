
package com.vinods.gildedrose.domain.service;

import com.vinods.gildedrose.Item;

/**
 * Domain service responsible for sell-in date manipulations.
 * Encapsulates all logic related to the sell-in date lifecycle.
 *
 * Demonstrates Single Responsibility Principle - this class has one reason to change:
 * changes to how sell-in dates are managed.
 */
public class SellInAdjuster {

    /**
     * Decrements the sell-in date by one day.
     *
     * @param item The item whose sell-in date should be decremented
     */
    public void decrementSellIn(Item item) {
        item.sellIn--;
    }

    /**
     * Checks if the item has passed its sell-by date.
     * An item is considered expired when sellIn is negative.
     *
     * @param item The item to check
     * @return true if the item has expired (sellIn < 0)
     */
    public boolean isExpired(Item item) {
        return item.sellIn < 0;
    }

    /**
     * Gets the number of days remaining until expiration.
     * Negative values indicate the item is already expired.
     *
     * @param item The item to check
     * @return The sellIn value
     */
    public int getDaysUntilExpiration(Item item) {
        return item.sellIn;
    }
}
