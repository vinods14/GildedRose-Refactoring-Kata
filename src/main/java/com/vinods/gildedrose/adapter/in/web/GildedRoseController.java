package com.vinods.gildedrose.adapter.in.web;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.adapter.in.web.dto.ItemRequest;
import com.vinods.gildedrose.adapter.in.web.dto.ItemResponse;
import com.vinods.gildedrose.adapter.in.web.mapper.ItemMapper;
import com.vinods.gildedrose.application.port.InventoryUpdateService;
import com.vinods.gildedrose.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Inventory", description = "Endpoints for managing Gilded Rose inventory updates")
public class GildedRoseController {

    private static final Logger log = LoggerFactory.getLogger(GildedRoseController.class);

    private final InventoryUpdateService inventoryUpdateService;
    private final ItemMapper itemMapper;

    public GildedRoseController(InventoryUpdateService inventoryUpdateService, ItemMapper itemMapper) {
        this.inventoryUpdateService = inventoryUpdateService;
        this.itemMapper = itemMapper;
    }

    @PostMapping("/update-quality")
    @Operation(
            summary = "Update inventory quality",
            description = "Advances the inventory by one day — decrements sell-in and adjusts quality "
                    + "for every item according to Gilded Rose business rules."
    )
    @ApiResponse(responseCode = "200", description = "Successfully updated items",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid item data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ItemResponse[]> updateQuality(@RequestBody ItemRequest[] requests) {
        log.info("Received update-quality request for {} item(s)", requests.length);
        Item[] items = itemMapper.toItems(requests);
        inventoryUpdateService.updateInventory(items);
        ItemResponse[] responses = itemMapper.toItemResponses(items);
        log.info("Successfully updated {} item(s)", responses.length);
        return ResponseEntity.ok(responses);
    }
}
