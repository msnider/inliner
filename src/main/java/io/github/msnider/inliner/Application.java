package io.github.msnider.inliner;

import io.github.msnider.inliner.utils.SSLUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {
	public static void main(String[] args) throws Exception {
		SSLUtils.setTrustAllCerts();
		SpringApplication.run(Application.class, args);
	}
}
