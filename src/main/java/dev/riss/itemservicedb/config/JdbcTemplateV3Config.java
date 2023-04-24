package dev.riss.itemservicedb.config;

import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.jdbctemplate.JdbcTemplateItemRepositoryV3;
import dev.riss.itemservicedb.service.ItemService;
import dev.riss.itemservicedb.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV3Config {

    private final DataSource dataSource;

    @Bean
    public ItemService itemService () {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository () {
        return new JdbcTemplateItemRepositoryV3(dataSource);
    }

}
