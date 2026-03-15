package com.vinods.gildedrose.application.service;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.common.exception.InvalidItemException;
import com.vinods.gildedrose.domain.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GildedRoseInventoryService.
 * Verifies orchestration logic and correct strategy selection for all item types.
 */
class GildedRoseInventoryServiceTest {

    private GildedRoseInventoryService service;

    @BeforeEach
    void setUp() {
        service = new GildedRoseInventoryService(List.of(
                new SulfurasUpdateStrategy(),
                new AgedBrieUpdateStrategy(),
                new BackstagePassUpdateStrategy(),
                new ConjuredItemUpdateStrategy(),
                new NormalItemUpdateStrategy()
        ));
    }

    // -------------------------------------------------------------------------
    // Orchestration tests
    // -------------------------------------------------------------------------

    @Test
    void updateInventory_handlesNullArray() {
        assertDoesNotThrow(() -> service.updateInventory(null));
    }

    @Test
    void updateInventory_handlesEmptyArray() {
        Item[] items = new Item[0];
        assertDoesNotThrow(() -> service.updateInventory(items));
    }

    @Test
    void updateInventory_updatesSingleItem() {
        Item[] items = {new Item("Normal Item", 10, 20)};

        service.updateInventory(items);

        assertEquals(19, items[0].quality);
        assertEquals(9, items[0].sellIn);
    }

    @Test
    void updateInventory_updatesMultipleItems() {
        Item[] items = {
                new Item("Normal Item", 10, 20),
                new Item("Aged Brie", 10, 20),
                new Item("Sulfuras, Hand of Ragnaros", 10, 80)
        };

        service.updateInventory(items);

        assertEquals(19, items[0].quality); // Normal: -1
        assertEquals(9, items[0].sellIn);

        assertEquals(21, items[1].quality);  // Aged Brie: +1
        assertEquals(9, items[1].sellIn);

        assertEquals(80, items[2].quality);  // Sulfuras: no change
        assertEquals(10, items[2].sellIn);
    }

    @Test
    void updateInventory_usesCorrectStrategyForEachItem() {
        Item[] items = {
                new Item("+5 Dexterity Vest", 10, 20),
                new Item("Aged Brie", 2, 0),
                new Item("Elixir of the Mongoose", 5, 7),
                new Item("Sulfuras, Hand of Ragnaros", 0, 80),
                new Item("Backstage passes to a TAFKAL80ETC concert", 15, 20)
        };

        service.updateInventory(items);

        assertEquals(19, items[0].quality); // Dexterity Vest: normal item
        assertEquals(1, items[1].quality);  // Aged Brie: increases
        assertEquals(6, items[2].quality);  // Elixir: normal item
        assertEquals(80, items[3].quality); // Sulfuras: never changes
        assertEquals(21, items[4].quality); // Backstage pass: increases by 1
    }

    @Test
    void updateInventory_appliesUpdatesInOrder() {
        Item[] items = {
                new Item("Item 1", 5, 10),
                new Item("Item 2", 5, 10),
                new Item("Item 3", 5, 10)
        };

        service.updateInventory(items);

        for (Item item : items) {
            assertEquals(9, item.quality);
            assertEquals(4, item.sellIn);
        }
    }

    @Test
    void updateInventory_canBeCalledMultipleTimes() {
        Item[] items = {new Item("Normal Item", 5, 10)};

        service.updateInventory(items); // Day 1
        service.updateInventory(items); // Day 2
        service.updateInventory(items); // Day 3

        assertEquals(7, items[0].quality);
        assertEquals(2, items[0].sellIn);
    }

    @Test
    void updateInventory_handlesAgedBrieNearMaxQuality() {
        Item[] items = {new Item("Aged Brie", 5, 49)};

        service.updateInventory(items);

        assertEquals(50, items[0].quality);
    }

    @Test
    void updateInventory_handlesNormalItemAtZeroQuality() {
        Item[] items = {new Item("Normal Item", 5, 0)};

        service.updateInventory(items);

        assertEquals(0, items[0].quality);
    }

    @Test
    void updateInventory_handlesBackstagePassAfterConcert() {
        Item[] items = {new Item("Backstage passes to a TAFKAL80ETC concert", 0, 50)};

        service.updateInventory(items);

        assertEquals(0, items[0].quality);
    }

    @Test
    void updateInventory_throwsInvalidItemException_forNullItem() {
        Item[] items = new Item[]{null};
        assertThrows(InvalidItemException.class, () -> service.updateInventory(items));
    }

    @Test
    void updateInventory_throwsInvalidItemException_forBlankItemName() {
        Item[] items = {new Item("", 5, 20)};
        assertThrows(InvalidItemException.class, () -> service.updateInventory(items));
    }

    // -------------------------------------------------------------------------
    // Strategy-selection tests (migrated from deleted ItemUpdateStrategyFactoryTest)
    // -------------------------------------------------------------------------

    @Test
    void selectsCorrectStrategy_forSulfuras() {
        Item[] items = {new Item("Sulfuras, Hand of Ragnaros", 5, 80)};
        service.updateInventory(items);
        // Sulfuras: quality and sellIn never change
        assertEquals(80, items[0].quality);
        assertEquals(5, items[0].sellIn);
    }

    @Test
    void selectsCorrectStrategy_forAgedBrie() {
        Item[] items = {new Item("Aged Brie", 5, 20)};
        service.updateInventory(items);
        // Aged Brie: quality increases
        assertEquals(21, items[0].quality);
        assertEquals(4, items[0].sellIn);
    }

    @Test
    void selectsCorrectStrategy_forBackstagePass() {
        Item[] items = {new Item("Backstage passes to a TAFKAL80ETC concert", 15, 20)};
        service.updateInventory(items);
        // Backstage pass > 10 days: +1
        assertEquals(21, items[0].quality);
    }

    @Test
    void selectsCorrectStrategy_forConjuredItem() {
        Item[] items = {new Item("Conjured Mana Cake", 5, 20)};
        service.updateInventory(items);
        // Conjured: quality decreases by 2
        assertEquals(18, items[0].quality);
    }

    @Test
    void selectsNormalStrategy_forUnknownItem() {
        Item[] items = {new Item("Unknown Item", 5, 20)};
        service.updateInventory(items);
        // Normal: quality decreases by 1
        assertEquals(19, items[0].quality);
    }

    @Test
    void selectsNormalStrategy_forStandardItems() {
        Item[] items = {
                new Item("+5 Dexterity Vest", 10, 20),
                new Item("Elixir of the Mongoose", 5, 7)
        };
        service.updateInventory(items);
        assertEquals(19, items[0].quality);
        assertEquals(6, items[1].quality);
    }

    @Test
    void caseMatters_forItemNames() {
        // Lowercase / uppercase variants are treated as normal items
        Item[] items = {
                new Item("aged brie", 5, 20),
                new Item("AGED BRIE", 5, 20)
        };
        service.updateInventory(items);
        assertEquals(19, items[0].quality); // normal degradation
        assertEquals(19, items[1].quality); // normal degradation
    }

    @Test
    void selectsConjuredStrategy_forAnyConjuredPrefix() {
        Item[] items = {new Item("Conjured anything", 5, 20)};
        service.updateInventory(items);
        // Conjured: quality decreases by 2
        assertEquals(18, items[0].quality);
    }
}
