package dev.riss.itemservicedb.config;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.memory.MemoryItemRepository;
import dev.riss.itemservicedb.service.ItemService;
import dev.riss.itemservicedb.service.ItemServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MemoryItemRepository();
    }

}
