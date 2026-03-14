package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.domain.service.QualityAdjuster;
import com.vinods.gildedrose.domain.service.SellInAdjuster;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Template Method pattern in BaseQualityUpdateStrategy.
 * Uses a concrete test implementation to verify the template method executes steps in correct order.
 */
class BaseQualityUpdateStrategyTest {

    @Test
    void templateMethod_executesStepsInCorrectOrder() {
        // Arrange
        QualityAdjuster qualityAdjuster = new QualityAdjuster();
        SellInAdjuster sellInAdjuster = new SellInAdjuster();
        TestStrategy strategy = new TestStrategy(qualityAdjuster, sellInAdjuster);
        Item item = new Item("Test Item", 10, 20);

        // Act
        strategy.updateQuality(item);

        // Assert - verify all steps were executed in order
        assertTrue(strategy.beforeSellInCalled);
        assertTrue(strategy.decrementSellInCalled);
        assertTrue(strategy.afterSellInCalled);

        // Verify execution order
        assertTrue(strategy.beforeSellInCallOrder < strategy.decrementSellInCallOrder);
        assertTrue(strategy.decrementSellInCallOrder < strategy.afterSellInCallOrder);
    }

    @Test
    void templateMethod_decrementsSellIn() {
        // Arrange
        QualityAdjuster qualityAdjuster = new QualityAdjuster();
        SellInAdjuster sellInAdjuster = new SellInAdjuster();
        TestStrategy strategy = new TestStrategy(qualityAdjuster, sellInAdjuster);
        Item item = new Item("Test Item", 10, 20);

        // Act
        strategy.updateQuality(item);

        // Assert - sellIn should be decremented
        assertEquals(9, item.sellIn);
    }

    @Test
    void templateMethod_allowsOverridingDecrementSellIn() {
        // Arrange
        QualityAdjuster qualityAdjuster = new QualityAdjuster();
        SellInAdjuster sellInAdjuster = new SellInAdjuster();
        NoDecrementStrategy strategy = new NoDecrementStrategy(qualityAdjuster, sellInAdjuster);
        Item item = new Item("Test Item", 10, 20);

        // Act
        strategy.updateQuality(item);

        // Assert - sellIn should NOT be decremented when overridden
        assertEquals(10, item.sellIn);
        assertTrue(strategy.overriddenDecrementCalled);
    }

    @Test
    void protectedMembers_accessibleToSubclasses() {
        // Arrange
        QualityAdjuster qualityAdjuster = new QualityAdjuster();
        SellInAdjuster sellInAdjuster = new SellInAdjuster();
        TestStrategy strategy = new TestStrategy(qualityAdjuster, sellInAdjuster);
        Item item = new Item("Test Item", 10, 20);

        // Act
        strategy.updateQuality(item);

        // Assert - subclass should have access to protected members
        assertNotNull(strategy.qualityAdjuster);
        assertNotNull(strategy.sellInAdjuster);
        assertSame(qualityAdjuster, strategy.qualityAdjuster);
        assertSame(sellInAdjuster, strategy.sellInAdjuster);
    }

    /**
     * Concrete test implementation to verify template method execution.
     */
    private static class TestStrategy extends BaseQualityUpdateStrategy {
        boolean beforeSellInCalled = false;
        boolean decrementSellInCalled = false;
        boolean afterSellInCalled = false;
        int beforeSellInCallOrder = 0;
        int decrementSellInCallOrder = 0;
        int afterSellInCallOrder = 0;
        private int callCounter = 0;

        TestStrategy(QualityAdjuster qualityAdjuster, SellInAdjuster sellInAdjuster) {
            super(qualityAdjuster, sellInAdjuster);
        }

        @Override
        protected void updateQualityBeforeSellIn(Item item) {
            beforeSellInCalled = true;
            beforeSellInCallOrder = ++callCounter;
        }

        @Override
        protected void decrementSellIn(Item item) {
            super.decrementSellIn(item);
            decrementSellInCalled = true;
            decrementSellInCallOrder = ++callCounter;
        }

        @Override
        protected void updateQualityAfterSellIn(Item item) {
            afterSellInCalled = true;
            afterSellInCallOrder = ++callCounter;
        }

        @Override
        public boolean canHandle(String itemName) {
            return true;
        }
    }

    /**
     * Test implementation that overrides decrementSellIn to verify it can be customized.
     */
    private static class NoDecrementStrategy extends BaseQualityUpdateStrategy {
        boolean overriddenDecrementCalled = false;

        NoDecrementStrategy(QualityAdjuster qualityAdjuster, SellInAdjuster sellInAdjuster) {
            super(qualityAdjuster, sellInAdjuster);
        }

        @Override
        protected void updateQualityBeforeSellIn(Item item) {
            // No-op for test
        }

        @Override
        protected void decrementSellIn(Item item) {
            // Override to NOT decrement (like Sulfuras)
            overriddenDecrementCalled = true;
        }

        @Override
        protected void updateQualityAfterSellIn(Item item) {
            // No-op for test
        }

        @Override
        public boolean canHandle(String itemName) {
            return true;
        }
    }
}
