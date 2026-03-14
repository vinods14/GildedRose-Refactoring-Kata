package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;

/**
 * Strategy interface for updating item quality and sell-in values.
 * This is a domain port that defines the contract for all item update behaviors.
 *
 * Implements Strategy Pattern and Open/Closed Principle:
 * - Closed for modification: interface is stable
 * - Open for extension: new strategies can be added without changing existing code
 *
 * Demonstrates Interface Segregation Principle:
 * - Small, focused interface with only the methods clients need
 */
public interface ItemUpdateStrategy {

    /**
     * Updates the quality and sell-in values of an item according to business rules.
     * Each strategy implements its own specific business logic.
     *
     * @param item The item to update (mutated in place per kata constraints)
     */
    void updateQuality(Item item);

    /**
     * Determines if this strategy can handle the given item name.
     * Used by the factory to select the appropriate strategy.
     *
     * @param itemName The name of the item
     * @return true if this strategy handles the item, false otherwise
     */
    boolean canHandle(String itemName);
}
