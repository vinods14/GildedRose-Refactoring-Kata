package com.vinods.gildedrose.controller;

import com.vinods.gildedrose.service.GildedRoseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class GildedRoseController {
    private final GildedRoseService service;

    public GildedRoseController(GildedRoseService service) {
        this.service = service;
    }

    @PostMapping("/update-quality")
    public com.gildedrose.Item[] updateQuality(@RequestBody com.gildedrose.Item[] items) {
        return service.updateQuality(items);
    }
}
