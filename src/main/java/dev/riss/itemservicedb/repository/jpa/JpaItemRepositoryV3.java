package dev.riss.itemservicedb.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.domain.QItem;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static dev.riss.itemservicedb.domain.QItem.*;

/**
 * QueryDsl
 * 컴파일 시점에서 SQL 오류를 다 잡아줌 -> JPQL 빌더 역할
 * 별도의 스프링 예외 추상화 지원안함.
 * 결국 JPA 빌더이기 때문에, 문제는 JPA 에서 다 발생함
 * 그러므로 @Repository 에서 다 해결됨
 */
@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;

    // 얘는 QueryDsl 꺼임. QueryDsl 은 JPA 의 JPQL 을 만들어주는 빌더 역할
    // (JPA 쿼리 만들어주는 공장) => JPAQueryFactory
    private final JPAQueryFactory query;

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
        // 설정 방식 JdbcTemplate 에 dataSource 넣는거랑 비슷
        // 얘도 밖에서 스프링빈으로 등록해서 사용해도 됨
    }

    // JPA 사용
    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    // JPA 사용
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    // JPA 사용
    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    // QueryDsl 사용 - 더 리펙토링된 방법
    // likeItemName, maxPrice 는 자바 코드이기 때문에 쿼리문 조각을 부분적으로 모듈화해서 재사용 가능
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        return query
                .select(item)
                .from(item)
                .where(likeItemName(itemName), maxPrice(maxPrice))        // , 로 그 뒤에 또 Expression 적으면 AND 가 됨
                .fetch();
    }

    private BooleanExpression likeItemName (String itemName) {
        if (StringUtils.hasText(itemName))
            return item.itemName.like("%" + itemName + "%");
        return null;        // return null 이면 where 문에서 무시됨 (where() 안에 null 이 들어가면 무시됨)
    }

    private Predicate maxPrice (Integer maxPrice) {
        if (null != maxPrice) return item.price.loe(maxPrice);
        return null;
    }

    // QueryDsl 사용 - 기본 방법
    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

//        QItem item=new QItem("i");      // "i" 가 alias
        // 근데 내부에 QItem.item 을 갖고 있으므로 QItem.item 으로 사용해도 됨
        //QItem item=QItem.item;

        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(itemName))
            builder.and(item.itemName.like("%" + itemName + "%"));

        if (null != maxPrice) builder.and(item.price.loe(maxPrice));    // loe => lessThanOrEqual

        List<Item> result = query.select(item)        // 위에서 처럼 item new 로 만들어서 사용도 가능
                .from(item)     //QItem.item 인데 static 이므로 static import 해줬음
                .where(builder)
                .fetch();

        return result;
    }
}
