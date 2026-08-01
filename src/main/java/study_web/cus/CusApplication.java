package study_web.cus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CusApplication {

	public static void main(String[] args) {
		SpringApplication.run(CusApplication.class, args);
	}

}
