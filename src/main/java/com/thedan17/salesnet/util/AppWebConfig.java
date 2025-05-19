package com.thedan17.salesnet.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppWebConfig  implements WebMvcConfigurer {
  private final UrlCountingInterceptor interceptor;

  public AppWebConfig(UrlCountingInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(interceptor)
            .addPathPatterns("/api/**");
  }

  @Override
  public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173") // или 3000, если CRA
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
  }
}
