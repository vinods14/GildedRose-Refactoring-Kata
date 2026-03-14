package com.vinods.gildedrose.application.service;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.application.factory.ItemUpdateStrategyFactory;
import com.vinods.gildedrose.common.exception.InvalidItemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GildedRoseInventoryService.
 * Verifies orchestration logic without testing business rules (which are in strategies).
 */
class GildedRoseInventoryServiceTest {

    private GildedRoseInventoryService service;

    @BeforeEach
    void setUp() {
        ItemUpdateStrategyFactory factory = new ItemUpdateStrategyFactory();
        service = new GildedRoseInventoryService(factory);
    }

    @Test
    void updateInventory_handlesNullArray() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> service.updateInventory(null));
    }

    @Test
    void updateInventory_handlesEmptyArray() {
        // Arrange
        Item[] items = new Item[0];

        // Act - should not throw exception
        assertDoesNotThrow(() -> service.updateInventory(items));
    }

    @Test
    void updateInventory_updatesSingleItem() {
        // Arrange
        Item[] items = {new Item("Normal Item", 10, 20)};

        // Act
        service.updateInventory(items);

        // Assert - quality should decrease, sellIn should decrement
        assertEquals(19, items[0].quality);
        assertEquals(9, items[0].sellIn);
    }

    @Test
    void updateInventory_updatesMultipleItems() {
        // Arrange
        Item[] items = {
                new Item("Normal Item", 10, 20),
                new Item("Aged Brie", 10, 20),
                new Item("Sulfuras, Hand of Ragnaros", 10, 80)
        };

        // Act
        service.updateInventory(items);

        // Assert
        assertEquals(19, items[0].quality); // Normal: -1
        assertEquals(9, items[0].sellIn);    // Normal: -1

        assertEquals(21, items[1].quality);  // Aged Brie: +1
        assertEquals(9, items[1].sellIn);    // Aged Brie: -1

        assertEquals(80, items[2].quality);  // Sulfuras: no change
        assertEquals(10, items[2].sellIn);   // Sulfuras: no change
    }

    @Test
    void updateInventory_usesCorrectStrategyForEachItem() {
        // Arrange
        Item[] items = {
                new Item("+5 Dexterity Vest", 10, 20),
                new Item("Aged Brie", 2, 0),
                new Item("Elixir of the Mongoose", 5, 7),
                new Item("Sulfuras, Hand of Ragnaros", 0, 80),
                new Item("Backstage passes to a TAFKAL80ETC concert", 15, 20)
        };

        // Act
        service.updateInventory(items);

        // Assert - verify each item was updated according to its rules
        assertEquals(19, items[0].quality); // Dexterity Vest: normal item
        assertEquals(1, items[1].quality);   // Aged Brie: increases
        assertEquals(6, items[2].quality);   // Elixir: normal item
        assertEquals(80, items[3].quality);  // Sulfuras: never changes
        assertEquals(21, items[4].quality);  // Backstage pass: increases by 1
    }

    @Test
    void updateInventory_appliesUpdatesInOrder() {
        // Arrange
        Item[] items = {
                new Item("Item 1", 5, 10),
                new Item("Item 2", 5, 10),
                new Item("Item 3", 5, 10)
        };

        // Act
        service.updateInventory(items);

        // Assert - all items should be updated
        for (Item item : items) {
            assertEquals(9, item.quality);
            assertEquals(4, item.sellIn);
        }
    }

    @Test
    void updateInventory_canBeCalledMultipleTimes() {
        // Arrange
        Item[] items = {new Item("Normal Item", 5, 10)};

        // Act - call multiple times
        service.updateInventory(items); // Day 1
        service.updateInventory(items); // Day 2
        service.updateInventory(items); // Day 3

        // Assert - should have degraded 3 times
        assertEquals(7, items[0].quality);
        assertEquals(2, items[0].sellIn);
    }

    @Test
    void updateInventory_handlesAgedBrieNearMaxQuality() {
        // Arrange
        Item[] items = {new Item("Aged Brie", 5, 49)};

        // Act
        service.updateInventory(items);

        // Assert - should cap at 50
        assertEquals(50, items[0].quality);
    }

    @Test
    void updateInventory_handlesNormalItemAtZeroQuality() {
        // Arrange
        Item[] items = {new Item("Normal Item", 5, 0)};

        // Act
        service.updateInventory(items);

        // Assert - should stay at 0
        assertEquals(0, items[0].quality);
    }

    @Test
    void updateInventory_handlesBackstagePassAfterConcert() {
        // Arrange
        Item[] items = {new Item("Backstage passes to a TAFKAL80ETC concert", 0, 50)};

        // Act
        service.updateInventory(items);

        // Assert - should drop to 0 after concert
        assertEquals(0, items[0].quality);
    }

    @Test
    void updateInventory_throwsInvalidItemException_forNullItem() {
        // Arrange
        Item[] items = new Item[]{null};

        // Act & Assert
        assertThrows(InvalidItemException.class, () -> service.updateInventory(items));
    }

    @Test
    void updateInventory_throwsInvalidItemException_forBlankItemName() {
        // Arrange
        Item[] items = {new Item("", 5, 20)};

        // Act & Assert
        assertThrows(InvalidItemException.class, () -> service.updateInventory(items));
    }
}
