package dev.riss.itemservicedb.repository.v2;

import dev.riss.itemservicedb.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
