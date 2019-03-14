package com.gala.sam.lambSauce.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderMatchingService {

  private static final String NAME = "Saucy";

  public String getName() {
    log.info("Returned name: {}", NAME);
    return NAME;
  }
}
