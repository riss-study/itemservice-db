package dev.riss.itemservicedb.config;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.jpa.JpaItemRepositoryV2;
import dev.riss.itemservicedb.repository.jpa.SpringDataJpaItemRepository;
import dev.riss.itemservicedb.service.ItemService;
import dev.riss.itemservicedb.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SpringDataJpaConfig {

    private final SpringDataJpaItemRepository repository;

    @Bean
    public ItemService itemService () {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository () {
        return new JpaItemRepositoryV2(repository);
    }

}
