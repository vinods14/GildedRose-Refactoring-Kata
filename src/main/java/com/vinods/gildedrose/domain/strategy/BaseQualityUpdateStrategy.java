package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.constants.QualityConstants;

/**
 * Abstract base class providing template method and shared functionality
 * for all item update strategies.
 *
 * Implements Template Method Pattern:
 * - Defines the skeleton of the update algorithm in updateQuality()
 * - Lets subclasses override specific steps without changing the algorithm structure
 *
 * Demonstrates:
 * - Inheritance: Subclasses inherit common behaviour
 * - Encapsulation: Protected members hide implementation details
 */
public abstract class BaseQualityUpdateStrategy implements ItemUpdateStrategy {

    /**
     * Template method defining the standard update algorithm.
     * This method is final to prevent subclasses from changing the algorithm structure.
     *
     * The algorithm follows three steps:
     * 1. Update quality based on current state (before sell-in changes)
     * 2. Decrement sell-in date (if applicable)
     * 3. Apply additional quality updates only if item is now expired
     *
     * @param item The item to update
     */
    @Override
    public final void updateQuality(Item item) {
        updateQualityBeforeSellIn(item);
        decrementSellIn(item);
        if (isExpired(item)) {
            updateQualityAfterSellIn(item);
        }
    }

    /**
     * Hook method: Update quality before decrementing sell-in.
     * Subclasses must implement this to define their specific quality update logic.
     *
     * @param item The item to update
     */
    protected abstract void updateQualityBeforeSellIn(Item item);

    /**
     * Hook method: Update quality after sell-in date passes (item is expired).
     * Only called when isExpired() returns true after decrementSellIn().
     *
     * @param item The item to update
     */
    protected abstract void updateQualityAfterSellIn(Item item);

    @Override
    public abstract boolean canHandle(String itemName);

    /**
     * Hook method: Decrement sell-in date.
     * Default implementation decrements by one.
     * Can be overridden (e.g., Sulfuras doesn't decrement).
     *
     * @param item The item whose sell-in should be decremented
     */
    protected void decrementSellIn(Item item) {
        item.sellIn--;
    }

    // -------------------------------------------------------------------------
    // Quality helpers — protected static so concrete strategies call them
    // without needing injected services.
    // -------------------------------------------------------------------------

    protected static void increaseQuality(Item item, int amount) {
        item.quality = Math.min(QualityConstants.MAX_QUALITY, item.quality + amount);
    }

    protected static void decreaseQuality(Item item, int amount) {
        item.quality = Math.max(QualityConstants.MIN_QUALITY, item.quality - amount);
    }

    protected static void setQuality(Item item, int value) {
        item.quality = Math.max(QualityConstants.MIN_QUALITY,
                Math.min(QualityConstants.MAX_QUALITY, value));
    }

    protected static boolean isExpired(Item item) {
        return item.sellIn < 0;
    }
}
