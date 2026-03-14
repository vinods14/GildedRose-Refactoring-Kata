package com.vinods.gildedrose.adapter.in.web.mapper;

import com.vinods.gildedrose.Item;
import com.vinods.gildedrose.adapter.in.web.dto.ItemRequest;
import com.vinods.gildedrose.adapter.in.web.dto.ItemResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toItem(ItemRequest request);

    ItemResponse toItemResponse(Item item);

    Item[] toItems(ItemRequest[] requests);

    ItemResponse[] toItemResponses(Item[] items);
}
