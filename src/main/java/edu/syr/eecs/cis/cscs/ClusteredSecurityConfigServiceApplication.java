package edu.syr.eecs.cis.cscs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class ClusteredSecurityConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClusteredSecurityConfigServiceApplication.class, args);
	}
}
