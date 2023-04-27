package dev.riss.itemservicedb.repository.jpa;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA 발생 예외: PersistenceException(JPA 예외)+그 하위 예외 + IllegalStateException, IllegalArgumentException
 * => 스프링 데이트 접근 예외 추상화가 안됨
 *    but, @Repository 애노테이션을 붙이면 스프링이 추상화한 예외로 변환되어 던져짐
 *    
 * @Repository 기능
 *  1. 컴포넌트 스캔의 대상됨
 *  2. 예외 변환 AOP 의 적용 대상이 됨 => 즉, 스프링이 예외 변환을 처리하는 AOP (Proxy) 를 만들어줌 (Transaction AOP Proxy 처럼)
 *      스프링과 JPA 를 함께 사용하는 경우, 스프링은 JPA 예외 변환기(PersistenceExceptionTranslator)를 등록함
 *      예외 변환 AOP 프록시는 JPA 관련 예외가 발생하면 JPA 예외 변환기를 통해 발생한 예외를 스프링 데이터 접근 예외로 변환
 *
 * @Repository 없을 때 (예외 변환 전)
 * 1. EntityManager - JPA 예외(PersistenceException) 발생
 * 2. JpaRepository 로 JPA 전달(PersistenceException)
 * 3. 레포지토리는 서비스 계층으로 JPA 전달(PersistenceException)
 * => 서비스 계층이 JPA 기술에 종속
 *
 * @Repository 있을 때 (예외 변환 후)
 * 1. EntityManager - JPA 예외(PersistenceException) 발생
 * 2. JpaRepository 로 JPA 전달(PersistenceException)
 * 3. 레포지토리가 '예외 변환 AOP Proxy' 로 JPA 전달(PersistenceException)
 * 4. 예외 변환 AOP Proxy 는 JPA 예외를 스프링 예외 추상화로 변환 (PersistenceException -> DataAccessException)
 * 5. Proxy 가 서비스 계층으로 스프링 예외 전달 (DataAccessException)
 * => 서비스 계층은 스프링 예외 추상화에 의존(DataAccessException)
 *
 * 실제 JPA 예외 변환 코드 => EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible()
 */
@Slf4j
@Repository
@Transactional(readOnly = true)
public class JpaItemRepositoryV1 implements ItemRepository {

    // dataSource 넣어주고 EntityManagerFactory 에서 EntityManager 꺼내서 써야하는데 (JpaTransactionManager 설정 등 많은 설정을 해야 함)
    // 스프링부트랑 통합하면 자동으로 해줌 (JpaBaseConfiguration 클래스 참조)
    private final EntityManager em;

    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public Item save(Item item) {
        em.persist(item);       // db 에 해당 item insert 쿼리 만들어서 넣어주고, id 값도 db 에서 받아와서 객체 필드에 넣어줌
        return item;
    }

    @Override
    @Transactional      // 보통 변경의 경우, 서비스계층에서 트랜잭션을 시작. 여기서는 복잡한 비즈니스 로직이 없기 때문에 repository 에서 걸음
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        // 내부에서 조회시점에 미리 스냅샷을 떠놓고(persistence context 에 저장)
        // 변경사항이 있으면 Transaction 이 커멋되는 시점에 변경사항을 기반으로 update query 를 만들어서 DB 에 날림 => dirty checking
        // 개인적으로 실무에서는 setter 빼고 비즈니스 로직을 엔티티 클래스에 비즈니스 메서드로 뽑는 게 나아보임
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        
        // test 에서 롤백 전에 업데이트 쿼리 확인하기 위해 추가해놈
        em.flush();
        em.clear();
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql="SELECT i FROM Item i";     // JPQL 은 테이블명이 아닌 엔티티를 기반(대상)으로 작성

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        if (StringUtils.hasText(itemName) || null != maxPrice) jpql+=" WHERE";

        boolean andFlag=false;
        List<Object> param = new ArrayList<>();

        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName LIKE CONCAT('%', :itemName, '%')";
            param.add(itemName);
            andFlag=true;
        }

        if (null != maxPrice) {
            if (andFlag) jpql+=" AND";
            jpql+=" i.price <= :maxPrice";
            param.add(maxPrice);
        }

        log.info("JPQL={}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) query.setParameter("itemName", itemName);
        if (null != maxPrice) query.setParameter("maxPrice", maxPrice);

        return query.getResultList();
    }
}
