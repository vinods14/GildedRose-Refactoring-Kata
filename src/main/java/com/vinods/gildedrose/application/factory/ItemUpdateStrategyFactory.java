package com.vinods.gildedrose.application.factory;

import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;
import com.vinods.gildedrose.domain.strategy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating and selecting appropriate item update strategies.
 * Uses Chain of Responsibility pattern to match item names to strategies.
 *
 * Demonstrates:
 * - Factory Pattern: Encapsulates object creation
 * - Chain of Responsibility: Iterates through strategies until match found
 * - Open/Closed Principle: Adding new item types requires only adding to strategy list
 * - Dependency Inversion: Returns abstractions (ItemUpdateStrategy), not concrete classes
 * - Single Responsibility: Only responsible for strategy selection
 */
public class ItemUpdateStrategyFactory {

    private static final Logger log = LoggerFactory.getLogger(ItemUpdateStrategyFactory.class);

    private final List<ItemUpdateStrategy> strategies;
    private final ItemUpdateStrategy defaultStrategy;

    /**
     * Default constructor that creates all strategies with default domain services.
     * Useful for production code.
     */
    public ItemUpdateStrategyFactory() {
        QualityAdjuster qualityAdjuster = new QualityAdjuster();
        SellInAdjuster sellInAdjuster = new SellInAdjuster();

        this.strategies = createStrategies(qualityAdjuster, sellInAdjuster);
        this.defaultStrategy = new NormalItemUpdateStrategy(qualityAdjuster, sellInAdjuster);
    }

    /**
     * Constructor with dependency injection for testing.
     * Allows mocking domain services.
     *
     * @param qualityAdjuster Service for quality manipulation
     * @param sellInAdjuster Service for sell-in date manipulation
     */
    public ItemUpdateStrategyFactory(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        this.strategies = createStrategies(qualityAdjuster, sellInAdjuster);
        this.defaultStrategy = new NormalItemUpdateStrategy(qualityAdjuster, sellInAdjuster);
    }

    /**
     * Creates the list of strategies in priority order.
     * Order matters - more specific strategies first.
     *
     * To add a new item type:
     * 1. Create new strategy class implementing ItemUpdateStrategy
     * 2. Add it to this list in appropriate position
     * 3. Done - no other changes needed!
     *
     * @param qualityAdjuster Service for quality manipulation
     * @param sellInAdjuster Service for sell-in date manipulation
     * @return List of strategies in priority order
     */
    private List<ItemUpdateStrategy> createStrategies(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        List<ItemUpdateStrategy> strategies = new ArrayList<>();

        // Order from most specific to least specific
        // More specific strategies should come first to avoid being shadowed

        strategies.add(new SulfurasUpdateStrategy(qualityAdjuster, sellInAdjuster));
        strategies.add(new AgedBrieUpdateStrategy(qualityAdjuster, sellInAdjuster));
        strategies.add(new BackstagePassUpdateStrategy(qualityAdjuster, sellInAdjuster));

        // Uncomment when implementing Conjured items feature:
        // strategies.add(new ConjuredItemUpdateStrategy(qualityAdjuster, sellInAdjuster));

        return strategies;
    }

    /**
     * Gets the appropriate strategy for the given item name.
     * Uses Chain of Responsibility pattern - iterates through strategies
     * until one returns true for canHandle().
     *
     * @param itemName The name of the item to update
     * @return The strategy that can handle this item, or default strategy
     */
    public ItemUpdateStrategy getStrategy(String itemName) {
        ItemUpdateStrategy strategy = strategies.stream()
                .filter(s -> s.canHandle(itemName))
                .findFirst()
                .orElse(defaultStrategy);
        log.debug("Selected strategy '{}' for item: '{}'", strategy.getClass().getSimpleName(), itemName);
        return strategy;
    }
}
