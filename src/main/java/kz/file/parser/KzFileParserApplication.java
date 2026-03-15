package kz.file.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class KzFileParserApplication {

  public static void main(String[] args) {
    SpringApplication.run(KzFileParserApplication.class, args);
  }

}
