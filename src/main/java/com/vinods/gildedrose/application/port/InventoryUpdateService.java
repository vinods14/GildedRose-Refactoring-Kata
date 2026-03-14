package com.vinods.gildedrose.application.port;

import com.vinods.gildedrose.Item;

/**
 * Input port for inventory update operations.
 * This is the hexagonal architecture port that the domain exposes to the outside world.
 *
 * In hexagonal architecture:
 * - This is an INPUT PORT (or "use case" interface)
 * - It defines how external adapters (like GildedRose) interact with the domain
 * - Implementations contain no business logic, only orchestration
 *
 * Demonstrates:
 * - Dependency Inversion Principle: High-level policy (adapters depend on this port)
 * - Interface Segregation Principle: Small, focused interface
 * - Hexagonal Architecture: Clear boundary between domain and external world
 */
public interface InventoryUpdateService {

    /**
     * Updates the quality and sell-in values for all items in the inventory.
     * Business rules are delegated to domain strategies, not implemented here.
     *
     * @param items Array of items to update
     */
    void updateInventory(Item[] items);
}
