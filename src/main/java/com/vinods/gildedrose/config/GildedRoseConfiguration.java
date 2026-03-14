package com.vinods.gildedrose.config;

import com.vinods.gildedrose.application.factory.ItemUpdateStrategyFactory;
import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.application.service.GildedRoseInventoryService;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires domain and application-service objects as beans.
 *
 * <p>This class is the <em>only</em> place where Spring annotations touch the
 * application internals. All domain and application-service classes remain plain
 * Java objects, keeping them framework-agnostic and easy to unit-test.
 *
 * <p>Wiring overview:
 * <pre>
 *   QualityAdjuster  ─┐
 *                     ├─► ItemUpdateStrategyFactory ─► GildedRoseInventoryService
 *   SellInAdjuster  ──┘                                       (as InventoryUpdateService port)
 * </pre>
 *
 * Demonstrates:
 * <ul>
 *   <li>Hexagonal Architecture: framework concern isolated to a single config class</li>
 *   <li>Dependency Inversion: the controller depends on the {@link InventoryUpdateService}
 *       port, not the concrete service implementation</li>
 * </ul>
 */
@Configuration
public class GildedRoseConfiguration {

    /**
     * Provides the domain service responsible for quality bound enforcement.
     *
     * @return a new {@link QualityAdjuster} instance
     */
    @Bean
    public QualityAdjuster qualityAdjuster() {
        return new QualityAdjuster();
    }

    /**
     * Provides the domain service responsible for sell-in date management.
     *
     * @return a new {@link SellInAdjuster} instance
     */
    @Bean
    public SellInAdjuster sellInAdjuster() {
        return new SellInAdjuster();
    }

    /**
     * Provides the strategy factory, injecting the domain services it needs.
     *
     * @param qualityAdjuster service for quality manipulation
     * @param sellInAdjuster  service for sell-in date manipulation
     * @return a configured {@link ItemUpdateStrategyFactory}
     */
    @Bean
    public ItemUpdateStrategyFactory itemUpdateStrategyFactory(
            QualityAdjuster qualityAdjuster,
            SellInAdjuster sellInAdjuster) {
        return new ItemUpdateStrategyFactory(qualityAdjuster, sellInAdjuster);
    }

    /**
     * Provides the primary input port implementation as a Spring bean.
     * Declared with the port type so dependents program against the interface.
     *
     * @param strategyFactory factory for item-type-specific update strategies
     * @return an {@link InventoryUpdateService} implementation
     */
    @Bean
    public InventoryUpdateService inventoryUpdateService(ItemUpdateStrategyFactory strategyFactory) {
        return new GildedRoseInventoryService(strategyFactory);
    }
}
