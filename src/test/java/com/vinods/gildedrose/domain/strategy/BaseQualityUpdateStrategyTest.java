package com.vinods.gildedrose.domain.strategy;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.constants.QualityConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Template Method pattern in BaseQualityUpdateStrategy and
 * the static quality/sell-in helpers (replacing deleted QualityAdjusterTest
 * and SellInAdjusterTest).
 */
class BaseQualityUpdateStrategyTest {

    // -------------------------------------------------------------------------
    // Template method tests
    // -------------------------------------------------------------------------

    @Test
    void templateMethod_executesStepsInCorrectOrder() {
        // Use sellIn=0 so the item expires after decrement (sellIn becomes -1)
        // and all three hooks are exercised.
        TestStrategy strategy = new TestStrategy();
        Item item = new Item("Test Item", 0, 20);

        strategy.updateQuality(item);

        assertTrue(strategy.beforeSellInCalled);
        assertTrue(strategy.decrementSellInCalled);
        assertTrue(strategy.afterSellInCalled);

        assertTrue(strategy.beforeSellInCallOrder < strategy.decrementSellInCallOrder);
        assertTrue(strategy.decrementSellInCallOrder < strategy.afterSellInCallOrder);
    }

    @Test
    void templateMethod_decrementsSellIn() {
        TestStrategy strategy = new TestStrategy();
        Item item = new Item("Test Item", 10, 20);

        strategy.updateQuality(item);

        assertEquals(9, item.sellIn);
    }

    @Test
    void templateMethod_doesNotCallAfterSellIn_whenNotExpired() {
        TestStrategy strategy = new TestStrategy();
        Item item = new Item("Test Item", 10, 20);

        strategy.updateQuality(item);

        assertTrue(strategy.beforeSellInCalled);
        assertTrue(strategy.decrementSellInCalled);
        assertFalse(strategy.afterSellInCalled);
    }

    @Test
    void templateMethod_callsAfterSellIn_whenExpiredAfterDecrement() {
        TestStrategy strategy = new TestStrategy();
        Item item = new Item("Test Item", 0, 20);  // sellIn becomes -1 after decrement

        strategy.updateQuality(item);

        assertTrue(strategy.afterSellInCalled);
    }

    @Test
    void templateMethod_allowsOverridingDecrementSellIn() {
        NoDecrementStrategy strategy = new NoDecrementStrategy();
        Item item = new Item("Test Item", 10, 20);

        strategy.updateQuality(item);

        assertEquals(10, item.sellIn);
        assertTrue(strategy.overriddenDecrementCalled);
    }

    // -------------------------------------------------------------------------
    // Static helper tests — increaseQuality
    // -------------------------------------------------------------------------

    @Test
    void increaseQuality_increasesWithinBounds() {
        Item item = new Item("Test", 5, 20);
        TestStrategy.increaseQuality(item, 5);
        assertEquals(25, item.quality);
    }

    @Test
    void increaseQuality_capsAtMaxQuality() {
        Item item = new Item("Test", 5, 48);
        TestStrategy.increaseQuality(item, 5);
        assertEquals(QualityConstants.MAX_QUALITY, item.quality);
    }

    @Test
    void increaseQuality_doesNotExceedMaxWhenAlreadyAtMax() {
        Item item = new Item("Test", 5, QualityConstants.MAX_QUALITY);
        TestStrategy.increaseQuality(item, 10);
        assertEquals(QualityConstants.MAX_QUALITY, item.quality);
    }

    // -------------------------------------------------------------------------
    // Static helper tests — decreaseQuality
    // -------------------------------------------------------------------------

    @Test
    void decreaseQuality_decreasesWithinBounds() {
        Item item = new Item("Test", 5, 20);
        TestStrategy.decreaseQuality(item, 5);
        assertEquals(15, item.quality);
    }

    @Test
    void decreaseQuality_stopsAtMinQuality() {
        Item item = new Item("Test", 5, 3);
        TestStrategy.decreaseQuality(item, 5);
        assertEquals(QualityConstants.MIN_QUALITY, item.quality);
    }

    @Test
    void decreaseQuality_doesNotGoNegativeWhenAlreadyAtMin() {
        Item item = new Item("Test", 5, 0);
        TestStrategy.decreaseQuality(item, 5);
        assertEquals(QualityConstants.MIN_QUALITY, item.quality);
    }

    // -------------------------------------------------------------------------
    // Static helper tests — setQuality
    // -------------------------------------------------------------------------

    @Test
    void setQuality_setsExactValueWithinBounds() {
        Item item = new Item("Test", 5, 20);
        TestStrategy.setQuality(item, 35);
        assertEquals(35, item.quality);
    }

    @Test
    void setQuality_clampsAboveMax() {
        Item item = new Item("Test", 5, 20);
        TestStrategy.setQuality(item, 100);
        assertEquals(QualityConstants.MAX_QUALITY, item.quality);
    }

    @Test
    void setQuality_clampsBelowMin() {
        Item item = new Item("Test", 5, 20);
        TestStrategy.setQuality(item, -10);
        assertEquals(QualityConstants.MIN_QUALITY, item.quality);
    }

    // -------------------------------------------------------------------------
    // Static helper tests — isExpired
    // -------------------------------------------------------------------------

    @Test
    void isExpired_returnsFalseWhenSellInPositive() {
        Item item = new Item("Test", 5, 20);
        assertFalse(TestStrategy.isExpired(item));
    }

    @Test
    void isExpired_returnsFalseWhenSellInZero() {
        Item item = new Item("Test", 0, 20);
        assertFalse(TestStrategy.isExpired(item));
    }

    @Test
    void isExpired_returnsTrueWhenSellInNegative() {
        Item item = new Item("Test", -1, 20);
        assertTrue(TestStrategy.isExpired(item));
    }

    @Test
    void isExpired_returnsTrueWellAfterExpiration() {
        Item item = new Item("Test", -10, 20);
        assertTrue(TestStrategy.isExpired(item));
    }

    // -------------------------------------------------------------------------
    // Test-only concrete implementations
    // -------------------------------------------------------------------------

    private static class TestStrategy extends BaseQualityUpdateStrategy {
        boolean beforeSellInCalled = false;
        boolean decrementSellInCalled = false;
        boolean afterSellInCalled = false;
        int beforeSellInCallOrder = 0;
        int decrementSellInCallOrder = 0;
        int afterSellInCallOrder = 0;
        private int callCounter = 0;

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

    private static class NoDecrementStrategy extends BaseQualityUpdateStrategy {
        boolean overriddenDecrementCalled = false;

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
