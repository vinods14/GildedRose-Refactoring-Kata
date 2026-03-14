package com.vinods.gildedrose;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GildedRoseTest {

    // ========== Normal Item Tests ==========

    @Test
    void normalItem_beforeSellDate_qualityDecreasesByOne() {
        Item[] items = new Item[] { new Item("+5 Dexterity Vest", 10, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(19, items[0].quality);
        assertEquals(9, items[0].sellIn);
    }

    @Test
    void normalItem_onSellDate_qualityDecreasesByTwo() {
        Item[] items = new Item[] { new Item("Elixir of the Mongoose", 0, 10) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(8, items[0].quality);
        assertEquals(-1, items[0].sellIn);
    }

    @Test
    void normalItem_afterSellDate_qualityDecreasesByTwo() {
        Item[] items = new Item[] { new Item("Elixir of the Mongoose", -5, 10) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(8, items[0].quality);
        assertEquals(-6, items[0].sellIn);
    }

    @Test
    void normalItem_qualityNeverNegative() {
        Item[] items = new Item[] { new Item("Elixir of the Mongoose", 5, 0) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    @Test
    void normalItem_qualityNeverNegativeAfterSellIn() {
        Item[] items = new Item[] { new Item("Elixir of the Mongoose", -1, 0) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].quality);
        assertEquals(-2, items[0].sellIn);
    }

    // ========== Aged Brie Tests ==========

    @Test
    void agedBrie_beforeSellDate_qualityIncreasesByOne() {
        Item[] items = new Item[] { new Item("Aged Brie", 5, 10) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(11, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    @Test
    void agedBrie_onSellDate_qualityIncreasesByTwo() {
        Item[] items = new Item[] { new Item("Aged Brie", 0, 10) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(12, items[0].quality);
        assertEquals(-1, items[0].sellIn);
    }

    @Test
    void agedBrie_afterSellDate_qualityIncreasesByTwo() {
        Item[] items = new Item[] { new Item("Aged Brie", -5, 10) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(12, items[0].quality);
        assertEquals(-6, items[0].sellIn);
    }

    @Test
    void agedBrie_qualityNeverExceedsFifty() {
        Item[] items = new Item[] { new Item("Aged Brie", 5, 50) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(50, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    @Test
    void agedBrie_qualityNeverExceedsFiftyAfterSellIn() {
        Item[] items = new Item[] { new Item("Aged Brie", -1, 49) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(50, items[0].quality);
        assertEquals(-2, items[0].sellIn);
    }

    // ========== Backstage Pass Tests ==========

    @Test
    void backstagePass_moreThan10Days_qualityIncreasesByOne() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 15, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(21, items[0].quality);
        assertEquals(14, items[0].sellIn);
    }

    @Test
    void backstagePass_exactly10Days_qualityIncreasesByTwo() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 10, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(22, items[0].quality);
        assertEquals(9, items[0].sellIn);
    }

    @Test
    void backstagePass_10DaysOrLess_qualityIncreasesByTwo() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 8, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(22, items[0].quality);
        assertEquals(7, items[0].sellIn);
    }

    @Test
    void backstagePass_exactly5Days_qualityIncreasesByThree() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 5, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(23, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    @Test
    void backstagePass_5DaysOrLess_qualityIncreasesByThree() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 3, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(23, items[0].quality);
        assertEquals(2, items[0].sellIn);
    }

    @Test
    void backstagePass_afterConcert_qualityBecomesZero() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 0, 20) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].quality);
        assertEquals(-1, items[0].sellIn);
    }

    @Test
    void backstagePass_longAfterConcert_qualityRemainsZero() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", -5, 0) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].quality);
        assertEquals(-6, items[0].sellIn);
    }

    @Test
    void backstagePass_qualityNeverExceedsFifty() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 5, 49) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(50, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    // ========== Sulfuras Tests ==========

    @Test
    void sulfuras_qualityNeverChanges() {
        Item[] items = new Item[] { new Item("Sulfuras, Hand of Ragnaros", 0, 80) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(80, items[0].quality);
    }

    @Test
    void sulfuras_sellInNeverChanges() {
        Item[] items = new Item[] { new Item("Sulfuras, Hand of Ragnaros", 0, 80) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].sellIn);
    }

    @Test
    void sulfuras_qualityNeverChangesBeforeSellIn() {
        Item[] items = new Item[] { new Item("Sulfuras, Hand of Ragnaros", 5, 80) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(80, items[0].quality);
        assertEquals(5, items[0].sellIn);
    }

    @Test
    void sulfuras_qualityNeverChangesAfterSellIn() {
        Item[] items = new Item[] { new Item("Sulfuras, Hand of Ragnaros", -1, 80) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(80, items[0].quality);
        assertEquals(-1, items[0].sellIn);
    }

    // ========== Edge Case Tests ==========

    @Test
    void qualityAtZero_doesNotGoNegativeForNormalItem() {
        Item[] items = new Item[] { new Item("Normal Item", 5, 0) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(0, items[0].quality);
    }

    @Test
    void qualityAtFifty_doesNotExceedForAgedBrie() {
        Item[] items = new Item[] { new Item("Aged Brie", 5, 50) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(50, items[0].quality);
    }

    @Test
    void qualityAtFifty_doesNotExceedForBackstagePass() {
        Item[] items = new Item[] { new Item("Backstage passes to a TAFKAL80ETC concert", 5, 50) };
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        assertEquals(50, items[0].quality);
    }

    // ========== Multi-Day Tests ==========

    @Test
    void normalItem_multipleDays_qualityDecreasesCorrectly() {
        Item[] items = new Item[] { new Item("Normal Item", 5, 10) };
        GildedRose app = new GildedRose(items);

        app.updateQuality(); // Day 1: sellIn=4, quality=9
        assertEquals(9, items[0].quality);
        assertEquals(4, items[0].sellIn);

        app.updateQuality(); // Day 2: sellIn=3, quality=8
        assertEquals(8, items[0].quality);
        assertEquals(3, items[0].sellIn);
    }

    @Test
    void agedBrie_multipleDays_qualityIncreasesCorrectly() {
        Item[] items = new Item[] { new Item("Aged Brie", 2, 10) };
        GildedRose app = new GildedRose(items);

        app.updateQuality(); // Day 1: sellIn=1, quality=11
        assertEquals(11, items[0].quality);

        app.updateQuality(); // Day 2: sellIn=0, quality=12
        assertEquals(12, items[0].quality);

        app.updateQuality(); // Day 3: sellIn=-1, quality=14 (increases by 2 after sellIn)
        assertEquals(14, items[0].quality);
    }

}
