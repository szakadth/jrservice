package hu.breona.jrservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class JrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JrServiceApplication.class, args);
	}

}
