package bootcamp.hibernate_practical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HibernatePracticalApplication {

	public static void main(String[] args) {
		SpringApplication.run(HibernatePracticalApplication.class, args);
	}

}
