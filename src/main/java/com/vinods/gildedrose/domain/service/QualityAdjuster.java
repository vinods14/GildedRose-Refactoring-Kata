package com.vinods.gildedrose.domain.service;

import com.vinods.gildedrose.Item;

import static com.vinods.gildedrose.constants.QualityConstants.MAX_QUALITY;
import static com.vinods.gildedrose.constants.QualityConstants.MIN_QUALITY;

/**
 * Domain service responsible for all quality adjustments.
 * Encapsulates quality bounds enforcement (0-50 for normal items).
 *
 * Demonstrates Single Responsibility Principle - this class has one reason to change:
 * changes to how quality bounds are enforced.
 */
public class QualityAdjuster {

    /**
     * Increases item quality by the specified amount, respecting max bounds.
     *
     * @param item The item whose quality should be increased
     * @param amount The amount to increase (positive integer)
     */
    public void increaseQuality(Item item, int amount) {
        int newQuality = item.quality + amount;
        item.quality = Math.min(newQuality, MAX_QUALITY);
    }

    /**
     * Decreases item quality by the specified amount, respecting min bounds.
     *
     * @param item The item whose quality should be decreased
     * @param amount The amount to decrease (positive integer)
     */
    public void decreaseQuality(Item item, int amount) {
        int newQuality = item.quality - amount;
        item.quality = Math.max(newQuality, MIN_QUALITY);
    }

    /**
     * Sets quality to a specific value, respecting bounds.
     *
     * @param item The item whose quality should be set
     * @param quality The desired quality value
     */
    public void setQuality(Item item, int quality) {
        item.quality = Math.max(MIN_QUALITY, Math.min(quality, MAX_QUALITY));
    }

    /**
     * Checks if quality is at maximum allowed value.
     *
     * @param item The item to check
     * @return true if quality is at or exceeds MAX_QUALITY
     */
    public boolean isAtMaxQuality(Item item) {
        return item.quality >= MAX_QUALITY;
    }

    /**
     * Checks if quality is at minimum allowed value.
     *
     * @param item The item to check
     * @return true if quality is at or below MIN_QUALITY
     */
    public boolean isAtMinQuality(Item item) {
        return item.quality <= MIN_QUALITY;
    }
}