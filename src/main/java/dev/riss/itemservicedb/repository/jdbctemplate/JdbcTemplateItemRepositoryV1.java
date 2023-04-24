package dev.riss.itemservicedb.repository.jdbctemplate;

import dev.riss.itemservicedb.domain.Item;
import dev.riss.itemservicedb.repository.ItemRepository;
import dev.riss.itemservicedb.repository.ItemSearchCond;
import dev.riss.itemservicedb.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTemplate
 *
 * 샘플 예제
 * 단건 조회 - 숫자 조회
 * int rowCount=jdbcTemplate.queryForObject("select count(*) from item", Integer.class);
 *
 * 단건 조회 - 숫자 조회, 파라미터 바인딩
 * int countOfItemNameRiss=jdbcTemplate.queryForObject("select count(*) from item where item_name=?", Integer.class, "riss");
 *
 * 단건 조회 - 문자 조회
 * String name=jdbcTemplate.queryForObject("select item_name from item where id=?", String.class, 12L);
 * 
 * 단건 조회 - 객체 조회 (itemRowMapper() 는 사전에 구현한 RowMapper method)
 * Item item=jdbcTemplate.queryForObject("select id, item_name, price, quantity from item where id=?", itemRowMapper(), 12L);
 *
 * 목록 조회 - 객체 조회
 * List<Item> item=jdbcTemplate.query("select id, item_name, price, quantity from item", itemRowMapper());
 *
 * EXECUTE (테이블 생성 DDL 등)
 * jdbcTemplate.execute("create table my_table (id integer, name varchar(1000)");
 *
 * 스토어드 프로시저 호출
 * jdbcTemplate.update("call SUPPORT.REFRESH_ACTORS_SUMMARY(?)", Long.valueOf(unionId));
 */
@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template=new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "INSERT INTO item(item_name, price, quantity) VALUES(?, ?, ?)";
        KeyHolder keyHolder=new GeneratedKeyHolder();

        // DB INSERT
        template.update(connection -> {
            // 자동 증가 키
            // keyHolder 를 지정해주고 아래 구문에서 new String[]{"id"} 를 뒤에 지정해주면
            // INSERT 쿼리 실행 이후에 db 에서 생성된 id 값 조회 가능
            PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"id"});
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            return pstmt;
        }, keyHolder);  // keyHolder 지정

        // id 값은 db 에서 만들어주는 거기 때문에 이 값을 select 하기 위해 KeyHolder 라는 것을 사용(JdbcTemplate)
        long key = keyHolder.getKey().longValue();
        item.setId(key);

        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "UPDATE item SET item_name=?, price=?, quantity=? WHERE id=?";

        // DB UPDATE
        template.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "SELECT id, item_name, price, quantity FROM item WHERE id = ?";
        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            // queryForObject 는 결과가 없으면 EmptyResultDataAccessException 예외가 터짐
            // 결과가 둘 이상이면 IncorrectResultSizeDataAccessException 예외가 발생
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql="SELECT id, item_name, price, quantity FROM item ";

        // 동적 쿼리
        if (StringUtils.hasText(itemName) || null != maxPrice) {
            sql += " WHERE";
        }

        boolean andFlag=false;
        List<Object> param = new ArrayList<>();

        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', ?, '%')";
            param.add(itemName);
            andFlag=true;
        }

        if (null != maxPrice) {
            if (andFlag) sql += " AND";
            sql += " price <= ?";
            param.add(maxPrice);
        }

        log.info("sql={}", sql);

        return template.query(sql, itemRowMapper(), param.toArray());
    }

    private RowMapper<Item> itemRowMapper() {
        return ((rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));

            return item;
        });
    }
}
