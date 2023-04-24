package dev.riss.itemservicedb.repository.jdbctemplate;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate
 *  - ? 대신 :xxx 로 파라미터명을 받음 => 파라미터 순서를 안맞춰도 됨
 *  - DB 가 생성해주는 키 (ex. save) 를 더 쉽게 조회할 수 있음
 *
 * parameter 로 자주 이용하는 방법 3가지
 * SqlParameterSource
 *  - 1. BeanPropertySqlParameterSource
 *  - 2. MapSqlParameterSource
 * 3. Map
 *
 * BeanPropertyRowMapper
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    // NamedParameterJdbcTemplate 도 관례상 DataSource 를 주입받고 내부에서 생성하는 방법을 많이 씀
    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template=new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "INSERT INTO item(item_name, price, quantity) VALUES(:itemName, :price, :quantity)";

        // 1. SqlParameterSource 구현체 중 BeanPropertySqlParameterSource 이용 하는 방법
        // 객체에 있는 변수명을 넘겨서 파라미터를 만듦 (:itemName 에는 item.getItemName() 이 바인딩)
        // => java bean property 규약 이용
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);

        KeyHolder keyHolder=new GeneratedKeyHolder();

        // DB INSERT
        template.update(sql, param, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);

        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "UPDATE item SET item_name=:itemName, price=:price, quantity=:quantity WHERE id=:id";

        // 2. SqlParameterSource 구현체 중 MapSqlParameterSource 를 이용하는 방법
        // 이렇게 각각 parameter 이름이랑(:xxx) 각 변수랑 매핑시켜서 직접 바인딩 (메서드 체이닝 제공)
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);
        // 여기선 ItemUpdateDto 객체에 id 값이 없고 itemId 값이 따로 들어오므로 BeanPropertySqlParameterSource 사용 불가능
        // MapSqlParameterSource 나 Map 을 이용해서 param 생성해야 함

        // DB UPDATE
        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "SELECT id, item_name, price, quantity FROM item WHERE id = :id";
        try {
            // 3. 자바 순수 문법 컬렉션 Map 이용하는 방법
            // NamedParameterJdbcTemplate 은 queryForObject 에 들어가는 파라미터랑, 로우매퍼 순서가 반대임 (<-> 그냥 JdbcTemplate)
            Map<String, Object> param = Map.of("id", id);   // hashMap 으로 만들어도 됨
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

        // save 에서 썼던 BeanPropertySqlParameterSource 이용
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
        // resultSet 갖고 column 명과 Item 객체에 있는 멤버변수 이름에 맞는 값을 매핑해서 다 넣어줌
        // 기존에 rs.getString("xxx") -> setXxx 어차피 xxx 는 같은 이름이므로 가능함
        // (여기도 자바빈프로퍼티 규약에 맞추어 데이터 변환, 실제로 내부에서는 리플렉션 같은 기능 사용)
        // camel 변환 지원(스네이크 -> 카멜 ex. item_name -> itemName ==> setItemName(rs.getString("item_name")) 자동으로 해줌)
        return BeanPropertyRowMapper.newInstance(Item.class);
    }
}
