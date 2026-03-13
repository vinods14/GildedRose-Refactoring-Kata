package com.gildedrose;

import org.springframework.stereotype.Service;

@Service
public class GildedRoseService {
    public Item[] updateQuality(Item[] items) {
        GildedRose app = new GildedRose(items);
        app.updateQuality();
        return app.items;
    }
}
