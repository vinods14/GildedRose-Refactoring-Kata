package com.vinods.gildedrose.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO representing an inventory item after its daily quality update")
public class ItemResponse {

    @Schema(description = "The name of the item", example = "Aged Brie")
    private String name;

    @Schema(description = "The number of days left to sell the item after update", example = "4")
    private int sellIn;

    @Schema(description = "The quality of the item after update", example = "21")
    private int quality;

    public ItemResponse() {}

    public ItemResponse(String name, int sellIn, int quality) {
        this.name = name;
        this.sellIn = sellIn;
        this.quality = quality;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSellIn() { return sellIn; }
    public void setSellIn(int sellIn) { this.sellIn = sellIn; }

    public int getQuality() { return quality; }
    public void setQuality(int quality) { this.quality = quality; }
}
