package dev.riss.itemservicedb;

import dev.riss.itemservicedb.config.MemoryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(MemoryConfig.class)
@SpringBootApplication
public class ItemserviceDbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemserviceDbApplication.class, args);
	}

}
