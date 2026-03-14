package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;

/**
 * Abstract base class providing template method and shared functionality
 * for all item update strategies.
 *
 * Implements Template Method Pattern:
 * - Defines the skeleton of the update algorithm in updateQuality()
 * - Lets subclasses override specific steps without changing the algorithm structure
 *
 * Demonstrates:
 * - Inheritance: Subclasses inherit common behavior
 * - Encapsulation: Protected members hide implementation details
 * - Dependency Inversion: Depends on abstractions (QualityAdjuster, SellInAdjuster)
 */
public abstract class BaseQualityUpdateStrategy implements ItemUpdateStrategy {

    /**
     * Domain service for quality adjustments.
     * Protected to allow subclasses to use it.
     */
    protected final QualityAdjuster qualityAdjuster;

    /**
     * Domain service for sell-in date adjustments.
     * Protected to allow subclasses to use it.
     */
    protected final SellInAdjuster sellInAdjuster;

    /**
     * Constructor with dependency injection.
     * Demonstrates Dependency Inversion Principle.
     *
     * @param qualityAdjuster Service for quality manipulation
     * @param sellInAdjuster Service for sell-in date manipulation
     */
    protected BaseQualityUpdateStrategy(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        this.qualityAdjuster = qualityAdjuster;
        this.sellInAdjuster = sellInAdjuster;
    }

    /**
     * Template method defining the standard update algorithm.
     * This method is final to prevent subclasses from changing the algorithm structure.
     *
     * The algorithm follows three steps:
     * 1. Update quality based on current state (before sell-in changes)
     * 2. Decrement sell-in date (if applicable)
     * 3. Apply additional quality updates if item is expired
     *
     * Subclasses customize behavior by overriding the hook methods.
     *
     * @param item The item to update
     */
    @Override
    public final void updateQuality(Item item) {
        updateQualityBeforeSellIn(item);
        decrementSellIn(item);
        updateQualityAfterSellIn(item);
    }

    /**
     * Hook method: Update quality before decrementing sell-in.
     * Subclasses must implement this to define their specific quality update logic.
     *
     * @param item The item to update
     */
    protected abstract void updateQualityBeforeSellIn(Item item);

    /**
     * Hook method: Decrement sell-in date.
     * Default implementation decrements by one.
     * Can be overridden (e.g., Sulfuras doesn't decrement).
     *
     * @param item The item whose sell-in should be decremented
     */
    protected void decrementSellIn(Item item) {
        sellInAdjuster.decrementSellIn(item);
    }

    /**
     * Hook method: Update quality after sell-in date passes (if applicable).
     * Subclasses must implement this to define behavior for expired items.
     *
     * @param item The item to update
     */
    protected abstract void updateQualityAfterSellIn(Item item);
}
