package dev.riss.itemservicedb.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity     // JPA 가 사용하는 객체라는 뜻. 이게 있어야 JPA 가 인식할 수 있음. => 이게 붙은 객체를 엔티티라고 함
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //Identity: DB 에서 id 값을 넣어주는 전략
    private Long id;

    @Column(name = "item_name", length = 10)        // 생략 가능. 없으면 필드명을 테이블 컬럼명으로 사용(스프링부트 통합 시 camel -> snake 자동 변환)
    private String itemName;
    private Integer price;
    private Integer quantity;

    // JPA 는 protect 이상 레벨의 기본 생성자 필수 (프록시 객체 생성 때문에 필요)
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
