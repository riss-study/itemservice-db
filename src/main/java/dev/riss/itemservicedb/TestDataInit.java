package dev.riss.itemservicedb;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {

    private final ItemRepository itemRepository;

    /**
     * 확인용 초기 데이터 추가
     */
    @EventListener(ApplicationReadyEvent.class) // 스프링 컨테이너가 완전히 초기화를 다 끝내고(AOP 등) 실행할 준비가 됐을 때 발생하는 이벤트
    // @PostConstruct 는 간혹 AOP 같은 부분이 다 처리되지 않은 시점에 호출될 가능성이 있음 (ex. @Transactional 관련 트랜잭션 AOP)
    // @EventListener 가 스프링 관련된 이벤트이므로 얘도 스프링빈에 등록돼있어야 함
    public void initData() {
        log.info("test data init");
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }

}
