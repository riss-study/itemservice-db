package dev.riss.itemservicedb.repository.mybatis;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * Mybatis 설정 원리
 * 1. 애플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈이 @Mapper 가 붙어 있는 인터페이스 조회 <<interface>> ItemMapper
 * 2. 인터페이스 발견되면, 동적 프록시 기술을 이용해서 인터페이스 구현체 생성(like AOP)  <<proxy>>ItemMapper 구현체 (동적 프록시 객체)
 * 3. 생성된 구현체를 스프링 빈으로 등록(스프링 컨테이너 안에)
 * => 실제 주입받는 Mapper 는 인터페이스가 아닌 동적 프록시 객체
 *    getClass() log => class jdk.proxy2.$Proxy66 찍힘
 *    (com.sun.proxy.$Proxy66 아니네..? jdk16 이상부터 강력한 캡슐화가 기본설정이라 변경됐다고 함)
 *    
 * 결론
 * -> 매퍼 구현체 덕분에 Mybatis 를 스프링에 편리하게 통합해서 사용 가능
 * -> 매퍼 구현체는 스프링 예외 추상화인 DataAccessException 에 맞게 예외도 변환해서 반환 (JdbcTemplate 처럼 스프링 예외 변환기 존재)
 * -> 마이바티스 스프링 연동 모듈이 DB 커넥션, 트랜잭션과 관련된 기능도 같이 연동, 동기화해줌
 *
 * 참고
 * -> Mybatis-Spring 연동 모듈이 자동 등록해주는 부분 => MybatisAutoconfiguration 클래스
 */
@Mapper     // 이 애노테이션이 있으면 마이바티스-스프링 모듈에서 자동 인식 후 구현체를 만들어내서(프록시 사용) 스프링 빈에 등록해줌. 의존 관계 주입이 됨.
public interface ItemMapper {

    void save (Item item);

    // @Param: 파라미터가 1개일 때는 생략 가능, 2개 이상일 때는 @Param("xml 에서 쓰일 파라미터 이름")
    void update (@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    List<Item> findAll (ItemSearchCond itemSearch);

    Optional<Item> findById (Long id);

}
