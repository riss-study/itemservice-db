package dev.riss.itemservicedb.config;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.mybatis.ItemMapper;
import dev.riss.itemservicedb.repository.mybatis.MybatisItemRepository;
import dev.riss.itemservicedb.service.ItemService;
import dev.riss.itemservicedb.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {

    private final ItemMapper itemMapper;
    // MyBatis 모듈이 dataSource 나 TransactionManager 같은 것들을 다 읽어서 Mapper 랑 다 연결시켜줌 (알아서 해줌)

    @Bean
    public ItemService itemService () {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository () {
        return new MybatisItemRepository(itemMapper);
    }

}
