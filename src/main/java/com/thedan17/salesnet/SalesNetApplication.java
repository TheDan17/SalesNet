package com.thedan17.salesnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Класс отправной точки запуска сервиса. */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SalesNetApplication {
  /** Главный метод, являющийся точкой запуска сервиса. */
  public static void main(String[] args) {
    SpringApplication.run(SalesNetApplication.class, args);
  }
}
