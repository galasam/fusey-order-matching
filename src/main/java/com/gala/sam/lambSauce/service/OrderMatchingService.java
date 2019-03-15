package com.gala.sam.lambSauce.service;

import com.gala.sam.lambSauce.domain.Order;
import com.gala.sam.lambSauce.domain.Trade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderMatchingService {

  final MarketService marketService;

  public List<Trade> getResultingTrades(List<Order> orders) {
    marketService.clear();
    orders.forEach(marketService::completeTimestep);
    return marketService.getAllMatchedTrades();
  }
}
