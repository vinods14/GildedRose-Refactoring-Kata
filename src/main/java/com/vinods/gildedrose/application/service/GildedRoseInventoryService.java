package com.vinods.gildedrose.application.service;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.common.exception.InvalidItemException;
import com.vinods.gildedrose.domain.strategy.ItemUpdateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Application service implementing the inventory update port.
 * Orchestrates domain logic without containing business rules.
 *
 * In hexagonal architecture:
 * - This is the IMPLEMENTATION of an input port
 * - It coordinates domain objects but contains NO business logic
 * - Business rules live in domain strategies, not here
 */
public class GildedRoseInventoryService implements InventoryUpdateService {

    private static final Logger log = LoggerFactory.getLogger(GildedRoseInventoryService.class);

    private final List<ItemUpdateStrategy> strategies;

    public GildedRoseInventoryService(List<ItemUpdateStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Updates all items in the inventory by delegating to appropriate strategies.
     * This method contains NO business logic - it only orchestrates.
     *
     * @param items Array of items to update (null-safe)
     */
    @Override
    public void updateInventory(Item[] items) {
        if (items == null) {
            log.info("Received null item array; skipping inventory update");
            return;
        }

        log.info("Starting inventory update for {} items", items.length);

        for (Item item : items) {
            updateSingleItem(item);
        }
    }

    /**
     * Updates a single item by selecting and executing appropriate strategy.
     * Validates that the item and its name are non-null and non-blank.
     *
     * @param item The item to update
     * @throws InvalidItemException if item is null or has a null/blank name
     */
    private void updateSingleItem(Item item) {
        if (item == null) {
            log.warn("Encountered null item in inventory array");
            throw new InvalidItemException("Item must not be null");
        }
        if (item.name == null || item.name.isBlank()) {
            log.warn("Encountered item with null or blank name");
            throw new InvalidItemException("Item name must not be null or blank");
        }
        log.debug("Updating item: '{}'", item.name);
        ItemUpdateStrategy strategy = getStrategy(item.name);
        strategy.updateQuality(item);
    }

    private ItemUpdateStrategy getStrategy(String itemName) {
        return strategies.stream()
                .filter(strategy -> strategy.canHandle(itemName))
                .findFirst()
                .orElseThrow(() -> new InvalidItemException("No strategy found for: " + itemName));
    }
}
