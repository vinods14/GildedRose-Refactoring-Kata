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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
 */
@WebMvcTest(GildedRoseController.class)
class GildedRoseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryUpdateService inventoryUpdateService;

    @MockBean
    private ItemMapper itemMapper;

    @Test
    void updateQuality_returnsTwoHundred_forValidItemArray() throws Exception {
        Item[] items = {new Item("Normal Item", 5, 20)};
        ItemResponse[] responses = {new ItemResponse("Normal Item", 4, 19)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);

        ItemRequest[] requests = {new ItemRequest("Normal Item", 5, 20)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuality_delegatesToInventoryUpdateService() throws Exception {
        Item[] items = {new Item("Aged Brie", 3, 10)};
        ItemResponse[] responses = {new ItemResponse("Aged Brie", 2, 11)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);

        ItemRequest[] requests = {new ItemRequest("Aged Brie", 3, 10)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk());

        verify(inventoryUpdateService).updateInventory(any(Item[].class));
    }

    @Test
    void updateQuality_returnsJsonArray() throws Exception {
        Item[] items = {new Item("Sulfuras, Hand of Ragnaros", 0, 80)};
        ItemResponse[] responses = {new ItemResponse("Sulfuras, Hand of Ragnaros", 0, 80)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);

        ItemRequest[] requests = {new ItemRequest("Sulfuras, Hand of Ragnaros", 0, 80)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateQuality_handlesMultipleItems() throws Exception {
        Item[] items = {
            new Item("Normal Item", 5, 20),
            new Item("Aged Brie", 3, 10),
            new Item("Sulfuras, Hand of Ragnaros", 0, 80)
        };
        ItemResponse[] responses = {
            new ItemResponse("Normal Item", 4, 19),
            new ItemResponse("Aged Brie", 2, 11),
            new ItemResponse("Sulfuras, Hand of Ragnaros", 0, 80)
        };
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);

        ItemRequest[] requests = {
            new ItemRequest("Normal Item", 5, 20),
            new ItemRequest("Aged Brie", 3, 10),
            new ItemRequest("Sulfuras, Hand of Ragnaros", 0, 80)
        };
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void updateQuality_returnsItemsWithOriginalNames() throws Exception {
        Item[] items = {new Item("Backstage passes to a TAFKAL80ETC concert", 10, 30)};
        ItemResponse[] responses = {new ItemResponse("Backstage passes to a TAFKAL80ETC concert", 9, 31)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(responses);

        ItemRequest[] requests = {new ItemRequest("Backstage passes to a TAFKAL80ETC concert", 10, 30)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Backstage passes to a TAFKAL80ETC concert"));
    }

    @Test
    void updateQuality_acceptsEmptyArray() throws Exception {
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(new Item[0]);
        when(itemMapper.toItemResponses(any(Item[].class))).thenReturn(new ItemResponse[0]);

        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returns400_whenInvalidItemExceptionThrown() throws Exception {
        Item[] items = {new Item("", 5, 20)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        doThrow(new InvalidItemException("Item name must not be null or blank"))
                .when(inventoryUpdateService).updateInventory(any(Item[].class));

        ItemRequest[] requests = {new ItemRequest("", 5, 20)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void returns400_forMalformedJson() throws Exception {
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns500_forUnexpectedException() throws Exception {
        Item[] items = {new Item("Normal Item", 5, 20)};
        when(itemMapper.toItems(any(ItemRequest[].class))).thenReturn(items);
        doThrow(new RuntimeException("Unexpected error"))
                .when(inventoryUpdateService).updateInventory(any(Item[].class));

        ItemRequest[] requests = {new ItemRequest("Normal Item", 5, 20)};
        mockMvc.perform(post("/api/items/update-quality")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
