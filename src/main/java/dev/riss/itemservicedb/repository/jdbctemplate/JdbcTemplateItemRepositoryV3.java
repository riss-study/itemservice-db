package dev.riss.itemservicedb.repository.jdbctemplate;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SimpleJdbcInsert - 나머지 코드는 안바뀌고 insert sql 부분에서만 도움되는 기능
 * (논외: SimpleJdbcCall -> 스토어드 프로시저를 편리하게 호출하는 기능)
 */
@Slf4j
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    // SimpleJdbcInsert 도 마찬가지로 dataSource 주입받고 내부에서 생성하는 관례법 사용 (당연히 스프링 빈으로 직접 등록하고 주입받아도 됨)
    // 근데 생성시 사용할 테이블명이나 key 값을 직접 등록하기 때문에, 가급적 얘는 빈 등록하지 않는 게 나음
    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        this.template=new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert=new SimpleJdbcInsert(dataSource)
                .withTableName("item")          // 테이블 명 지정
                .usingGeneratedKeyColumns("id");        // db 에서 자동으로 생성되는 key(PK 컬럼)명 있으면 지정
//                .usingColumns("item_name", "price", "quantity") // insert 에 사용할 컬럼명 지정. 생략 가능
//                (생성 시점에 dataSource 를 통해 테이블명, key 값 등을 가지고, DB 에서 메타데이터를 읽고 어떤 컬럼이 있는지 자동으로 인지함)
        // 모든 컬럼이 아닌 특정 컬럼만 지정해서 저장하고 싶다면 usingColumns 사용 하면 됨
    }

    @Override
    public Item save(Item item) {
        // DB INSERT
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        // db 테이블명만 알면 메타데이터로 어떤 컬럼이 있는지 인지할 수 있어서 매칭되는 value 값(javaBeans Property naming)만 넣어주면 이렇게 단순하게 가능
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "UPDATE item SET item_name=:itemName, price=:price, quantity=:quantity WHERE id=:id";

        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        // DB UPDATE
        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "SELECT id, item_name, price, quantity FROM item WHERE id = :id";
        try {
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        String sql="SELECT id, item_name, price, quantity FROM item ";

        // 동적 쿼리
        if (StringUtils.hasText(itemName) || null != maxPrice) {
            sql += " WHERE";
        }

        boolean andFlag=false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', :itemName, '%')";
            andFlag=true;
        }

        if (null != maxPrice) {
            if (andFlag) sql += " AND";
            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);

        return template.query(sql, param, itemRowMapper());
    }

    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);
    }
}
