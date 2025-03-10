package com.thedan17.salesnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Класс отправной точки запуска сервиса. */
@SpringBootApplication
public class SalesNetApplication {
  /** Главный метод, являющийся точкой запуска сервиса. */
  public static void main(String[] args) {
    SpringApplication.run(SalesNetApplication.class, args);
  }
}
