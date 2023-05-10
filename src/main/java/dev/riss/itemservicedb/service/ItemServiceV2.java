package dev.riss.itemservicedb.service;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import dev.riss.itemservicedb.repository.v2.ItemQueryRepositoryV2;
import dev.riss.itemservicedb.repository.v2.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceV2 implements ItemService {

    private final ItemRepositoryV2 itemRepositoryV2;
    private final ItemQueryRepositoryV2 itemQueryRepositoryV2;

    @Transactional(readOnly = true)
    @Override
    public Item save(Item item) {
        return itemRepositoryV2.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = itemRepositoryV2.findById(itemId).orElseThrow();

        // setter 계속 쓰기 귀찮아서 update 비즈니스 로직 생성
        findItem.updateItem(updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Item> findById(Long id) {
        return itemRepositoryV2.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Item> findItems(ItemSearchCond itemSearch) {
        return itemQueryRepositoryV2.findAll(itemSearch);
    }
}
