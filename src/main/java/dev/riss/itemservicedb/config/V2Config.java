package dev.riss.itemservicedb.config;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.jpa.JpaItemRepositoryV3;
import dev.riss.itemservicedb.repository.v2.ItemQueryRepositoryV2;
import dev.riss.itemservicedb.repository.v2.ItemRepositoryV2;
import dev.riss.itemservicedb.service.ItemService;
import dev.riss.itemservicedb.service.ItemServiceV2;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class V2Config {

    private final ItemRepositoryV2 itemRepositoryV2;  // SpringDataJpa 가 알아서 빈으로 등록해서 제공해줌
    private final EntityManager em;

    @Bean
    public ItemService itemService () {
        return new ItemServiceV2(itemRepositoryV2, itemQueryRepositoryV2());
    }

    @Bean
    public ItemQueryRepositoryV2 itemQueryRepositoryV2 () {
        return new ItemQueryRepositoryV2(em);
    }

    //TestInit 에서 사용하기 때문에, 등록해줌
    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }

}
