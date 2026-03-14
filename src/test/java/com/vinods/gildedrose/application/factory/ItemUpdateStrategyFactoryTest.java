package com.vinods.gildedrose.application.factory;

import com.vinods.gildedrose.domain.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ItemUpdateStrategyFactory.
 * Verifies correct strategy selection for all item types.
 */
class ItemUpdateStrategyFactoryTest {

    private ItemUpdateStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ItemUpdateStrategyFactory();
    }

    @Test
    void getStrategy_returnsSulfurasStrategy_forSulfuras() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy("Sulfuras, Hand of Ragnaros");

        // Assert
        assertInstanceOf(SulfurasUpdateStrategy.class, strategy);
        assertTrue(strategy.canHandle("Sulfuras, Hand of Ragnaros"));
    }

    @Test
    void getStrategy_returnsAgedBrieStrategy_forAgedBrie() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy("Aged Brie");

        // Assert
        assertInstanceOf(AgedBrieUpdateStrategy.class, strategy);
        assertTrue(strategy.canHandle("Aged Brie"));
    }

    @Test
    void getStrategy_returnsBackstagePassStrategy_forBackstagePasses() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy("Backstage passes to a TAFKAL80ETC concert");

        // Assert
        assertInstanceOf(BackstagePassUpdateStrategy.class, strategy);
        assertTrue(strategy.canHandle("Backstage passes to a TAFKAL80ETC concert"));
    }

    @Test
    void getStrategy_returnsNormalItemStrategy_forUnknownItem() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy("Unknown Item");

        // Assert
        assertInstanceOf(NormalItemUpdateStrategy.class, strategy);
        assertTrue(strategy.canHandle("Unknown Item"));
    }

    @Test
    void getStrategy_returnsNormalItemStrategy_forStandardItems() {
        // Arrange
        String[] standardItems = {
                "+5 Dexterity Vest",
                "Elixir of the Mongoose",
                "Random Potion",
                "Magic Sword"
        };

        // Act & Assert
        for (String itemName : standardItems) {
            ItemUpdateStrategy strategy = factory.getStrategy(itemName);
            assertInstanceOf(NormalItemUpdateStrategy.class, strategy,
                    "Expected NormalItemUpdateStrategy for: " + itemName);
        }
    }

    @Test
    void getStrategy_returnsNormalItemStrategy_forNullItemName() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy(null);

        // Assert
        assertInstanceOf(NormalItemUpdateStrategy.class, strategy);
    }

    @Test
    void getStrategy_returnsNormalItemStrategy_forEmptyString() {
        // Act
        ItemUpdateStrategy strategy = factory.getStrategy("");

        // Assert
        assertInstanceOf(NormalItemUpdateStrategy.class, strategy);
    }

    @Test
    void getStrategy_caseMattersForItemNames() {
        // Act
        ItemUpdateStrategy strategy1 = factory.getStrategy("aged brie"); // lowercase
        ItemUpdateStrategy strategy2 = factory.getStrategy("AGED BRIE"); // uppercase
        ItemUpdateStrategy strategy3 = factory.getStrategy("Aged Brie"); // correct case

        // Assert
        assertInstanceOf(NormalItemUpdateStrategy.class, strategy1,
                "Lowercase 'aged brie' should not match");
        assertInstanceOf(NormalItemUpdateStrategy.class, strategy2,
                "Uppercase 'AGED BRIE' should not match");
        assertInstanceOf(AgedBrieUpdateStrategy.class, strategy3,
                "Correct case 'Aged Brie' should match");
    }

    @Test
    void getStrategy_returnsSameStrategyTypeForSameItemName() {
        // Act
        ItemUpdateStrategy strategy1 = factory.getStrategy("Aged Brie");
        ItemUpdateStrategy strategy2 = factory.getStrategy("Aged Brie");

        // Assert - both should be same type (not necessarily same instance)
        assertEquals(strategy1.getClass(), strategy2.getClass());
    }

    @Test
    void getStrategy_returnsStrategyThatCanHandleTheItem() {
        // Arrange
        String[] testItems = {
                "Sulfuras, Hand of Ragnaros",
                "Aged Brie",
                "Backstage passes to a TAFKAL80ETC concert",
                "+5 Dexterity Vest"
        };

        // Act & Assert
        for (String itemName : testItems) {
            ItemUpdateStrategy strategy = factory.getStrategy(itemName);
            assertTrue(strategy.canHandle(itemName),
                    "Strategy should be able to handle: " + itemName);
        }
    }
}
