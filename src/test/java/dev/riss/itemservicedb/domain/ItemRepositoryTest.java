package dev.riss.itemservicedb.domain;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import dev.riss.itemservicedb.repository.memory.MemoryItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 테스트 원칙 -> 테스트는 다른 테스트와 격리돼있어야함, 테스트는 반복해서 실행할 수 있어야 함
@SpringBootTest
@Transactional      // 테스트 코드에서는 테스트를 트랜잭션 안에서 실행, 테스트 끝나면 트랜잭션 롤백 기능 추가 제공. 개발자가 직접 롤백해주지 않아도 됨
// 여기에 @Transactional 이 있고 service, repository class 안에도 @Transactional 이 있더라도, 이전의 트랜잭션 범위에 들어감
// (해당 트랜잭션에 참여하는 개념, 즉 같은 커넥션 사용) => 트랜잭션 전파 개념
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

/*    @Autowired
    PlatformTransactionManager transactionManager;
    TransactionStatus status;

    @BeforeEach
    void beforeEach () {
        // 모든 테스트 시작 전에 transaction start
        this.status=transactionManager.getTransaction(new DefaultTransactionDefinition());
    }*/

    @AfterEach
    void afterEach() {
        //MemoryItemRepository 의 경우 제한적으로 사용
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }
        // transaction rollback
//        transactionManager.rollback(status);
    }

/*    // 만약 해당 메서드에서는 테스트하더라도 롤백하지 않고 값을 커밋해서 보고 싶을 때는
    @Transactional
    @Commit    // 혹은 @Rollback(value = false) 써도 됨
    // 위의 두개를 추가해주면 됨*/
    @Test
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    @Test
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }

    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).containsExactly(items);
    }
}
