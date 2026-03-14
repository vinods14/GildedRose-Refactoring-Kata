package com.vinods.gildedrose;

import com.vinods.gildedrose.application.factory.ItemUpdateStrategyFactory;
import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.application.service.GildedRoseInventoryService;

/**
 * External API adapter for the Gilded Rose inventory system.
 * This class is the hexagonal architecture's INPUT ADAPTER.
 * It maintains backward compatibility with existing code.
 *
 * In hexagonal architecture, this is the "driving" adapter that:
 * - Receives requests from the external world (API/UI/CLI)
 * - Delegates to the domain via the InventoryUpdateService port
 * - Contains NO business logic (only translation/delegation)
 *
 * Demonstrates:
 * - Hexagonal Architecture: Adapter pattern for external interface
 * - Dependency Inversion: Depends on port abstraction, not implementation
 * - Single Responsibility: Only adapter responsibility (translation)
 * - Open/Closed: Business logic changes don't affect this adapter
 *
 * Refactored from 51-line monolithic method to clean, maintainable adapter.
 * Reduced cyclomatic complexity from 15+ to 1.
 */
class GildedRose {
    Item[] items;

    private final InventoryUpdateService inventoryService;

    /**
     * Default constructor maintaining backward compatibility.
     * Creates service dependencies internally for existing code.
     *
     * @param items Array of items to manage
     */
    public GildedRose(Item[] items) {
        this.items = items;
        // Wire up dependencies - in production, use DI framework
        ItemUpdateStrategyFactory factory = new ItemUpdateStrategyFactory();
        this.inventoryService = new GildedRoseInventoryService(factory);
    }

    /**
     * Constructor with dependency injection for testing.
     * Allows mocking the inventory service.
     *
     * @param items Array of items to manage
     * @param inventoryService Service for inventory updates
     */
    GildedRose(Item[] items, InventoryUpdateService inventoryService) {
        this.items = items;
        this.inventoryService = inventoryService;
    }

    /**
     * Updates quality and sell-in values for all items.
     * Delegates to application service - contains NO business logic.
     *
     * Before refactoring: 51 lines of nested conditionals
     * After refactoring: 1 line delegating to service
     */
    public void updateQuality() {
        inventoryService.updateInventory(items);
    }
}
