package com.vinods.gildedrose.config;

import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.application.service.GildedRoseInventoryService;
import com.vinods.gildedrose.domain.strategy.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration that wires domain and application-service objects as beans.
 *
 * <p>This class is the <em>only</em> place where Spring annotations touch the
 * application internals. All domain and application-service classes remain plain
 * Java objects, keeping them framework-agnostic and easy to unit-test.
 *
 * <p>This is the composition root: all concrete strategy classes are registered here
 * in priority order (most-specific first, NormalItemUpdateStrategy last as default).
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

    @Bean
    public InventoryUpdateService inventoryUpdateService() {
        return new GildedRoseInventoryService(List.of(
                new SulfurasUpdateStrategy(),
                new AgedBrieUpdateStrategy(),
                new BackstagePassUpdateStrategy(),
                new ConjuredItemUpdateStrategy(),
                new NormalItemUpdateStrategy()  // default — always canHandle()
        ));
    }
}
