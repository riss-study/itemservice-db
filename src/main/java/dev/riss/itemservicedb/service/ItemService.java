package dev.riss.itemservicedb.service;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    Item save(Item item);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findItems(ItemSearchCond itemSearch);
}
