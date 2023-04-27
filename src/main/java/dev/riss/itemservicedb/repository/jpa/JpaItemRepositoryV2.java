package dev.riss.itemservicedb.repository.jpa;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

// 스프링 데이터 JPA 는 스프링 추상화된 데이터 예외 변환을 자동으로 해주므로 @Repository 가 없어도 스프링 데이터 예외로 변환됨
@Repository
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        return repository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = repository.findById(itemId).orElseThrow();
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        // 실무에선 이렇게 안하고 동적 쿼리로 처리 (queryDsl 추천)
        if (StringUtils.hasText(itemName) && null != maxPrice) return repository.findItems("%"+itemName+"%", maxPrice);
        else if (StringUtils.hasText(itemName)) return repository.findByItemNameLike("%"+itemName+"%");
        else if (null != maxPrice) return repository.findByPriceLessThanEqual(maxPrice);
        return repository.findAll();
    }
}
