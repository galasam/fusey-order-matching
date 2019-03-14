package com.gala.sam.lambSauce.entrypoint;


import com.gala.sam.lambSauce.service.OrderMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FileEntryPoint {

  final OrderMatchingService orderMatchingService;

  public void processTradesFromFile() {
    log.info("Processing trades from file");
    log.info("Contacting Order Matching Service: {}", orderMatchingService.getName());
  }

}
