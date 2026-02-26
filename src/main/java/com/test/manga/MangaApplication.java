package com.test.manga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MangaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MangaApplication.class, args);

        System.out.println("   Acesse: http://localhost:8080     ");
        System.out.println("************************************");
        System.out.println("  Manga OCR Service Iniciado com Sucesso!  ");
        System.out.println("************************************");
        
        

    }

}
