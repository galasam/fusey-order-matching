package com.gala.sam.lambSauce;

import com.gala.sam.lambSauce.entrypoint.FileEntryPoint;
import com.gala.sam.lambSauce.service.OrderMatchingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {


  @Bean
  public OrderMatchingService orderMatchingService() {
    return new OrderMatchingService();
  }

  @Bean
  public FileEntryPoint fileEntryPoint() {
    return new FileEntryPoint(orderMatchingService());
  }
}
