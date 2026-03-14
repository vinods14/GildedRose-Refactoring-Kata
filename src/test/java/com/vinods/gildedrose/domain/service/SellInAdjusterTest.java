package com.vinods.gildedrose.domain.service;

import com.vinods.gildedrose.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SellInAdjusterTest {

    private SellInAdjuster sellInAdjuster;

    @BeforeEach
    void setUp() {
        sellInAdjuster = new SellInAdjuster();
    }

    @Test
    void decrementSellIn_decrementsByOne() {
        Item item = new Item("Test Item", 10, 20);
        sellInAdjuster.decrementSellIn(item);
        assertEquals(9, item.sellIn);
    }

    @Test
    void decrementSellIn_canGoNegative() {
        Item item = new Item("Test Item", 0, 20);
        sellInAdjuster.decrementSellIn(item);
        assertEquals(-1, item.sellIn);
    }

    @Test
    void decrementSellIn_multipleDecrements() {
        Item item = new Item("Test Item", 5, 20);
        sellInAdjuster.decrementSellIn(item);
        sellInAdjuster.decrementSellIn(item);
        sellInAdjuster.decrementSellIn(item);
        assertEquals(2, item.sellIn);
    }

    @Test
    void isExpired_returnsFalseBeforeExpiration() {
        Item item = new Item("Test Item", 5, 20);
        assertFalse(sellInAdjuster.isExpired(item));
    }

    @Test
    void isExpired_returnsFalseOnExpirationDay() {
        Item item = new Item("Test Item", 0, 20);
        assertFalse(sellInAdjuster.isExpired(item));
    }

    @Test
    void isExpired_returnsTrueAfterExpiration() {
        Item item = new Item("Test Item", -1, 20);
        assertTrue(sellInAdjuster.isExpired(item));
    }

    @Test
    void isExpired_returnsTrueWellAfterExpiration() {
        Item item = new Item("Test Item", -10, 20);
        assertTrue(sellInAdjuster.isExpired(item));
    }

    @Test
    void getDaysUntilExpiration_returnsPositiveValueBeforeExpiration() {
        Item item = new Item("Test Item", 5, 20);
        assertEquals(5, sellInAdjuster.getDaysUntilExpiration(item));
    }

    @Test
    void getDaysUntilExpiration_returnsZeroOnExpirationDay() {
        Item item = new Item("Test Item", 0, 20);
        assertEquals(0, sellInAdjuster.getDaysUntilExpiration(item));
    }

    @Test
    void getDaysUntilExpiration_returnsNegativeValueAfterExpiration() {
        Item item = new Item("Test Item", -3, 20);
        assertEquals(-3, sellInAdjuster.getDaysUntilExpiration(item));
    }
}
