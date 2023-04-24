package dev.riss.itemservicedb;

import dev.riss.itemservicedb.config.JdbcTemplateV1Config;
import dev.riss.itemservicedb.config.JdbcTemplateV2Config;
import dev.riss.itemservicedb.config.JdbcTemplateV3Config;
import dev.riss.itemservicedb.config.MemoryConfig;
import dev.riss.itemservicedb.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)		// 해당 Config 를 설정 파일로 사용한다는 애노테이션 (여기선 DB 기술교체마다 이걸로 바꿔쓸거임)
//@Import(JdbcTemplateV2Config.class)
@Import(JdbcTemplateV3Config.class)
@SpringBootApplication(scanBasePackages = "dev.riss.itemservicedb.web")		// web 패키지 하위만 컴포넌트스캔하겠다는 의미
// 그럼 나머지는 직접 수동으로 빈 등록할 예정
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

}
