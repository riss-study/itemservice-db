package dev.riss.itemservicedb;

import dev.riss.itemservicedb.config.*;
import dev.riss.itemservicedb.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)		// 해당 Config 를 설정 파일로 사용한다는 애노테이션 (여기선 DB 기술교체마다 이걸로 바꿔쓸거임)
//@Import(JdbcTemplateV2Config.class)
//@Import(JdbcTemplateV3Config.class)
//@Import(MyBatisConfig.class)
//@Import(JpaConfig.class)
//@Import(SpringDataJpaConfig.class)
@Import(QuerydslConfig.class)
@SpringBootApplication(scanBasePackages = "dev.riss.itemservicedb.web")		// web 패키지 하위만 컴포넌트스캔하겠다는 의미
// 그럼 나머지는 직접 수동으로 빈 등록할 예정
@Slf4j
public class ItemserviceDbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemserviceDbApplication.class, args);
	}

	@Bean		// 앞서 설명한 TestDataInit 클래스를 빈으로 등록함. 그래야 @EvenListener 가 호출됨
	@Profile("local")		// "local" 이라는 특정 프로필이 활성화된 경우에만 해당 클래스를 스프링 빈으로 등록한다는 뜻
	// properties 파일에 가보면 프로필 local 로 활성화해놨음
	public TestDataInit testDataInit (ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

/*
	// 하지만 스프링부트는 별다른 설정이 없으면, 테스트에서는 임베디드 모드를 자동으로 해줌
	@Bean
	@Profile("test")		// profile 이 "test" 인 경우(테스트 케이스에서만), dataSource 를 직접 빈 등록 (for 임베디드 DB)
	public DataSource dataSource() {
		log.info("메모리 데이터베이스 초기화");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.h2.Driver");			// h2 db Driver 사용하겠다는 뜻. h2 는 jvm 내에 db 메모리 모드 지원
		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");	// jdbc:h2:mem:db => 메모리 모드(임베디드 모드) DB 사용 설정
																// JVM 내에 database 를 만들고, 그곳에 데이터를 쏨
																// DB_CLOSE_DELAY=-1 => 임베디드 모드에서는 db 커넥션 연결이 모두 끊어지면,
																// 데이터베이스도 종료되는데 그것을 방지하는 설정
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		return dataSource;
	}*/

}
