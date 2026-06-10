package br.gov.sifap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SifapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SifapApplication.class, args);
    }
}
