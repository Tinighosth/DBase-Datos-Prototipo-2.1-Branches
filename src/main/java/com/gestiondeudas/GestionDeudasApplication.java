package com.gestiondeudas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GestionDeudasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionDeudasApplication.class, args);
    }
}
