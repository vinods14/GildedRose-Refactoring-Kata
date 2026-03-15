package com.vinods.gildedrose.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.adapter.in.web.dto.ItemRequest;
import com.vinods.gildedrose.adapter.in.web.dto.ItemResponse;
import com.vinods.gildedrose.adapter.in.web.mapper.ItemMapper;
import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.common.exception.InvalidItemException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link GildedRoseController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer (no full Spring context),
 * with {@code @MockBean} standing in for the {@link InventoryUpdateService} port
 * and {@link ItemMapper} (generated {@code @Component} not loaded by {@code @WebMvcTest}).
 * {@link GlobalExceptionHandler} is a {@code @RestControllerAdvice} and is loaded automatically.
 *
 * <p>Every test loads its JSON body from a fixture file under
 * {@code src/test/resources/fixtures/<scenario>/request.json} and asserts the full
 * response body against {@code …/response.json}, keeping the API contract in
 * version-controlled, human-readable files rather than inline strings.
 */
@WebMvcTest(GildedRoseController.class)
class GildedRoseControllerTest {

    private static final String UPDATE_QUALITY_URL = "/api/items/update-quality";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryUpdateService inventoryUpdateService;

    @MockBean
    private ItemMapper itemMapper;

    // -------------------------------------------------------------------------
    // Normal item
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_decreasesQualityAndSellInByOne_forNormalItemBeforeSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Normal Item", 4, 19)});

        // Act & Assert
        // fixtures/normal-item-before-sell-date/request.json  →  response.json
        performAndExpect("normal-item-before-sell-date", 200, true);
    }

    @Test
    void updateQuality_decreasesQualityByTwo_forNormalItemAfterSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Normal Item", -1, 8)});

        // Act & Assert
        // fixtures/normal-item-after-sell-date/request.json  →  response.json
        performAndExpect("normal-item-after-sell-date", 200, true);
    }

    @Test
    void updateQuality_qualityNeverDropsBelowZero_forNormalItemAtMinQuality() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Normal Item", 4, 0)});

        // Act & Assert
        // fixtures/normal-item-min-quality/request.json  →  response.json
        performAndExpect("normal-item-min-quality", 200, true);
    }

    // -------------------------------------------------------------------------
    // Aged Brie
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_increasesQualityByOne_forAgedBrieBeforeSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Aged Brie", 2, 11)});

        // Act & Assert
        // fixtures/aged-brie-before-sell-date/request.json  →  response.json
        performAndExpect("aged-brie-before-sell-date", 200, true);
    }

    @Test
    void updateQuality_increasesQualityByTwo_forAgedBrieAfterSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Aged Brie", -1, 12)});

        // Act & Assert
        // fixtures/aged-brie-after-sell-date/request.json  →  response.json
        performAndExpect("aged-brie-after-sell-date", 200, true);
    }

    @Test
    void updateQuality_qualityNeverExceedsFifty_forAgedBrieAtMaxQuality() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Aged Brie", 2, 50)});

        // Act & Assert
        // fixtures/aged-brie-max-quality/request.json  →  response.json
        performAndExpect("aged-brie-max-quality", 200, true);
    }

    // -------------------------------------------------------------------------
    // Sulfuras
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_neverChangesQualityOrSellIn_forSulfuras() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Sulfuras, Hand of Ragnaros", 0, 80)});

        // Act & Assert
        // fixtures/sulfuras/request.json  →  response.json
        performAndExpect("sulfuras", 200, true);
    }

    // -------------------------------------------------------------------------
    // Backstage passes
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_increasesQualityByOne_forBackstagePassWithMoreThanTenDays() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{
                new ItemResponse("Backstage passes to a TAFKAL80ETC concert", 14, 21)});

        // Act & Assert
        // fixtures/backstage-pass-more-than-ten-days/request.json  →  response.json
        performAndExpect("backstage-pass-more-than-ten-days", 200, true);
    }

    @Test
    void updateQuality_increasesQualityByTwo_forBackstagePassWithTenDaysOrLess() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{
                new ItemResponse("Backstage passes to a TAFKAL80ETC concert", 9, 22)});

        // Act & Assert
        // fixtures/backstage-pass-ten-days-or-less/request.json  →  response.json
        performAndExpect("backstage-pass-ten-days-or-less", 200, true);
    }

    @Test
    void updateQuality_increasesQualityByThree_forBackstagePassWithFiveDaysOrLess() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{
                new ItemResponse("Backstage passes to a TAFKAL80ETC concert", 4, 23)});

        // Act & Assert
        // fixtures/backstage-pass-five-days-or-less/request.json  →  response.json
        performAndExpect("backstage-pass-five-days-or-less", 200, true);
    }

    @Test
    void updateQuality_setsQualityToZero_forBackstagePassAfterConcert() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{
                new ItemResponse("Backstage passes to a TAFKAL80ETC concert", -1, 0)});

        // Act & Assert
        // fixtures/backstage-pass-after-concert/request.json  →  response.json
        performAndExpect("backstage-pass-after-concert", 200, true);
    }

    // -------------------------------------------------------------------------
    // Conjured items
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_decreasesQualityByTwo_forConjuredItemBeforeSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Conjured Mana Cake", 4, 8)});

        // Act & Assert
        // fixtures/conjured-before-sell-date/request.json  →  response.json
        performAndExpect("conjured-before-sell-date", 200, true);
    }

    @Test
    void updateQuality_decreasesQualityByFour_forConjuredItemAfterSellDate() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Conjured Mana Cake", -1, 6)});

        // Act & Assert
        // fixtures/conjured-after-sell-date/request.json  →  response.json
        performAndExpect("conjured-after-sell-date", 200, true);
    }

    // -------------------------------------------------------------------------
    // Mixed batch — all five types in a single request
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_returnsCorrectResponseForEachItem_forMixedBatch() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{
                new ItemResponse("Normal Item", 4, 19),
                new ItemResponse("Aged Brie", 2, 11),
                new ItemResponse("Sulfuras, Hand of Ragnaros", 0, 80),
                new ItemResponse("Backstage passes to a TAFKAL80ETC concert", 4, 23),
                new ItemResponse("Conjured Mana Cake", 4, 8)
        });

        // Act & Assert
        // fixtures/mixed-batch/request.json  →  response.json
        performAndExpect("mixed-batch", 200, true);
    }

    // -------------------------------------------------------------------------
    // Empty array
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_returnsEmptyArray_forEmptyRequest() throws Exception {
        // Arrange
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(new Item[0]);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(new ItemResponse[0]);

        // Act & Assert
        // fixtures/empty-array/request.json  →  response.json
        performAndExpect("empty-array", 200, true);
    }

    // -------------------------------------------------------------------------
    // Service delegation
    // -------------------------------------------------------------------------

    @Test
    void updateQuality_delegatesToInventoryUpdateService() throws Exception {
        // Arrange
        stubMapper(new ItemResponse[]{new ItemResponse("Normal Item", 4, 19)});

        // Act
        mockMvc.perform(post(UPDATE_QUALITY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readFixture("normal-item-before-sell-date", "request")))
                .andExpect(status().isOk());

        // Assert
        verify(inventoryUpdateService).updateInventory(any(Item[].class));
    }

    // -------------------------------------------------------------------------
    // Error paths
    // -------------------------------------------------------------------------

    @Test
    void returns400_withErrorBody_whenBlankItemNameThrowsInvalidItemException() throws Exception {
        // Arrange
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(new Item[]{new Item("", 5, 20)});
        doThrow(new InvalidItemException("Item name must not be null or blank"))
                .when(inventoryUpdateService).updateInventory(any(Item[].class));

        // Act & Assert
        // fixtures/error-blank-item-name/request.json  →  response.json  (lenient: timestamp excluded)
        performAndExpect("error-blank-item-name", 400, false);
    }

    @Test
    void returns400_withErrorBody_whenNullItemThrowsInvalidItemException() throws Exception {
        // Arrange
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(new Item[]{null});
        doThrow(new InvalidItemException("Item must not be null"))
                .when(inventoryUpdateService).updateInventory(any(Item[].class));

        // Act & Assert
        // fixtures/error-null-item-name/request.json  →  response.json  (lenient: timestamp excluded)
        performAndExpect("error-null-item-name", 400, false);
    }

    @Test
    void returns400_withErrorBody_forMalformedJson() throws Exception {
        // Arrange — no mapper stub needed; Jackson fails before reaching the service

        // Act & Assert
        // fixtures/error-malformed-json/request.json  →  response.json  (lenient: timestamp excluded)
        performAndExpect("error-malformed-json", 400, false);
    }

    @Test
    void returns500_withErrorBody_forUnexpectedException() throws Exception {
        // Arrange
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(new Item[]{new Item("Normal Item", 5, 20)});
        doThrow(new RuntimeException("Unexpected error"))
                .when(inventoryUpdateService).updateInventory(any(Item[].class));

        // Act & Assert
        // fixtures/error-unexpected/request.json  →  response.json  (lenient: timestamp excluded)
        performAndExpect("error-unexpected", 500, false);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Performs a POST to {@value UPDATE_QUALITY_URL} with the fixture's {@code request.json},
     * asserts the HTTP status, and checks the response body against {@code response.json}.
     *
     * @param scenario       name of the folder under {@code src/test/resources/fixtures/}
     * @param expectedStatus expected HTTP status code (e.g. 200, 400, 500)
     * @param strict         {@code true} for an exact JSON match (happy-path arrays);
     *                       {@code false} for lenient matching (error responses omit the
     *                       dynamic {@code timestamp} field)
     */
    private void performAndExpect(String scenario, int expectedStatus, boolean strict) throws Exception {
        String requestBody  = readFixture(scenario, "request");
        String expectedBody = readFixture(scenario, "response");

        mockMvc.perform(post(UPDATE_QUALITY_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().json(expectedBody, strict));
    }

    /**
     * Reads {@code src/test/resources/fixtures/<scenario>/<file>.json} from the classpath.
     */
    private String readFixture(String scenario, String file) throws IOException {
        ClassPathResource resource = new ClassPathResource(
                "fixtures/" + scenario + "/" + file + ".json");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }

    /**
     * Stubs both mapper methods. {@code toItems()} returns placeholder {@link Item} objects
     * (their exact values are irrelevant because the service is also mocked).
     * {@code toItemResponses()} returns the given {@code responses}, which the controller
     * serialises directly to JSON.
     */
    private void stubMapper(ItemResponse[] responses) {
        Item[] domainItems = new Item[responses.length];
        for (int i = 0; i < responses.length; i++) {
            ItemResponse r = responses[i];
            domainItems[i] = new Item(r.getName(), r.getSellIn(), r.getQuality());
        }
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(domainItems);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);
    }
}
