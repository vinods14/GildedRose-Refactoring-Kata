package com.vinods.gildedrose.domain.service;

import com.vinods.gildedrose.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QualityAdjusterTest {

    private QualityAdjuster qualityAdjuster;

    @BeforeEach
    void setUp() {
        qualityAdjuster = new QualityAdjuster();
    }

    @Test
    void increaseQuality_increasesQualityByAmount() {
        Item item = new Item("Test Item", 10, 20);
        qualityAdjuster.increaseQuality(item, 5);
        assertEquals(25, item.quality);
    }

    @Test
    void increaseQuality_doesNotExceedMaxQuality() {
        Item item = new Item("Test Item", 10, 48);
        qualityAdjuster.increaseQuality(item, 5);
        assertEquals(50, item.quality);
    }

    @Test
    void increaseQuality_cannotGoAbove50() {
        Item item = new Item("Test Item", 10, 50);
        qualityAdjuster.increaseQuality(item, 10);
        assertEquals(50, item.quality);
    }

    @Test
    void decreaseQuality_decreasesQualityByAmount() {
        Item item = new Item("Test Item", 10, 20);
        qualityAdjuster.decreaseQuality(item, 5);
        assertEquals(15, item.quality);
    }

    @Test
    void decreaseQuality_doesNotGoBelowMinQuality() {
        Item item = new Item("Test Item", 10, 3);
        qualityAdjuster.decreaseQuality(item, 5);
        assertEquals(0, item.quality);
    }

    @Test
    void decreaseQuality_cannotGoNegative() {
        Item item = new Item("Test Item", 10, 0);
        qualityAdjuster.decreaseQuality(item, 5);
        assertEquals(0, item.quality);
    }

    @Test
    void setQuality_setsExactValueWithinBounds() {
        Item item = new Item("Test Item", 10, 20);
        qualityAdjuster.setQuality(item, 35);
        assertEquals(35, item.quality);
    }

    @Test
    void setQuality_capsAtMaxWhenValueTooHigh() {
        Item item = new Item("Test Item", 10, 20);
        qualityAdjuster.setQuality(item, 100);
        assertEquals(50, item.quality);
    }

    @Test
    void setQuality_capsAtMinWhenValueTooLow() {
        Item item = new Item("Test Item", 10, 20);
        qualityAdjuster.setQuality(item, -10);
        assertEquals(0, item.quality);
    }

    @Test
    void isAtMaxQuality_returnsTrueWhenAtMax() {
        Item item = new Item("Test Item", 10, 50);
        assertTrue(qualityAdjuster.isAtMaxQuality(item));
    }

    @Test
    void isAtMaxQuality_returnsFalseWhenBelowMax() {
        Item item = new Item("Test Item", 10, 49);
        assertFalse(qualityAdjuster.isAtMaxQuality(item));
    }

    @Test
    void isAtMinQuality_returnsTrueWhenAtMin() {
        Item item = new Item("Test Item", 10, 0);
        assertTrue(qualityAdjuster.isAtMinQuality(item));
    }

    @Test
    void isAtMinQuality_returnsFalseWhenAboveMin() {
        Item item = new Item("Test Item", 10, 1);
        assertFalse(qualityAdjuster.isAtMinQuality(item));
    }
}
