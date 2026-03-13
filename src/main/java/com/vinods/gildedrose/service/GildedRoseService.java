package com.vinods.gildedrose.service;

import com.gildedrose.Item;
import com.vinods.gildedrose.GildedRose;
import org.springframework.stereotype.Service;

@Service
public class GildedRoseService {
    public Item[] updateQuality(Item[] items) {
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        return app.items;
    }
}
