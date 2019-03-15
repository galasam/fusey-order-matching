package com.gala.sam.lambSauce.service;

import static com.gala.sam.lambSauce.utils.MarketUtils.queueIfTimeInForce;

import com.gala.sam.lambSauce.domain.LimitOrder;
import com.gala.sam.lambSauce.domain.MarketOrder;
import com.gala.sam.lambSauce.domain.Order;
import com.gala.sam.lambSauce.domain.ReadyOrder;
import com.gala.sam.lambSauce.domain.ReadyOrder.DIRECTION;
import com.gala.sam.lambSauce.domain.StopOrder;
import com.gala.sam.lambSauce.domain.Trade;
import com.gala.sam.lambSauce.domain.dataStructures.TickerData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MarketService {

  private List<Trade> trades = new ArrayList<>();
  private Map<String, TickerData> tickerQueues = new TreeMap<>();
  private List<StopOrder> stopOrders = new LinkedList<>();

  public void clear() {
    trades = new ArrayList<>();
    tickerQueues = new TreeMap<>();
    stopOrders = new LinkedList<>();
  }

  public void completeTimestep(Order order) {
    log.info("Processing Triggered Stop Orders");
    processOrder(order);
    processTriggeredStopOrders();
  }

  private void processOrder(Order order) {
    log.info(String.format("Processing order %s", order.toString()));
    if(order instanceof StopOrder) {
      stopOrders.add((StopOrder) order);
    } else if(order instanceof LimitOrder) {
      processLimitOrder((LimitOrder) order);
    } else if(order instanceof MarketOrder) {
      processMarketOrder((MarketOrder) order);
    } else {
      throw new UnsupportedOperationException("Order type not specified");
    }
    log.info("Ticker queues: " + tickerQueues.toString());
    log.info("Stop Orders: " + stopOrders.toString());
    log.info("Trades: " + trades.toString());
  }

  private void processTriggeredStopOrders() {
    Iterator<StopOrder> it = stopOrders.iterator();
    while(it.hasNext()) {
      StopOrder stopOrder = it.next();
      log.info("Testing Trigger on: " + stopOrder.toString());
      if(isStopLossTriggered(stopOrder)) {
        log.info("Stop Order Triggered");
        it.remove();
        ReadyOrder readyOrder = stopOrder.getReadyOrder();
        processOrder(readyOrder);
      } else {
        log.info("Stop Order not Triggered");
      }
    }
  }

  private boolean isStopLossTriggered(StopOrder stopOrder) {
    ReadyOrder readyOrder = stopOrder.getReadyOrder();
    Optional<Float> lastExec = getTickerQueueGroup(readyOrder).getLastExecutedTradePrice();
    log.debug("Checking if there has been a previous trade");
    if(lastExec.isPresent()) {
      log.debug("Previous trade found, checking direction");
      if(readyOrder.getDirection().equals(DIRECTION.BUY)) {
        log.debug("Buy direction: testing trigger");
        return stopOrder.getTriggerPrice() <= lastExec.get();
      } else if(readyOrder.getDirection().equals(DIRECTION.SELL)) {
        log.debug("Sell direction: testing trigger");
        return stopOrder.getTriggerPrice() >= lastExec.get();
      } else {
        throw new UnsupportedOperationException("Order direction not supported");
      }
    } else {
      log.debug("No previous trade found");
      return false;
    }
  }

  private void processMarketOrder(MarketOrder marketOrder) {
    TickerData tickerData = getTickerQueueGroup(marketOrder);
    if (marketOrder.getDirection() == DIRECTION.BUY) {
      processDirectedMarketOrder(marketOrder, tickerData,
          tickerData.getSellLimitOrders(),tickerData.getBuyMarketOrders());
    } else if (marketOrder.getDirection() == DIRECTION.SELL) {
      processDirectedMarketOrder(marketOrder, tickerData,
          tickerData.getBuyLimitOrders(), tickerData.getSellMarketOrders());
    } else {
      throw new UnsupportedOperationException("Order direction not supported");
    }
  }

  private TickerData getTickerQueueGroup(ReadyOrder marketOrder) {
    TickerData queues = tickerQueues.get(marketOrder.getTicker());
    if(queues == null) {
      queues = new TickerData();
      tickerQueues.put(marketOrder.getTicker(), queues);
    }
    return queues;
  }

  private void processDirectedMarketOrder(MarketOrder marketOrder, TickerData tickerData,
      SortedSet<LimitOrder> limitOrders, SortedSet<MarketOrder> marketOrders) {
    log.debug("Checking Limit Order queue");
    if(limitOrders.isEmpty()) {
      log.debug("Limit Order queue empty, so check if time in force");
      queueIfTimeInForce(marketOrder, marketOrders);
    } else {
      LimitOrder limitOrder = limitOrders.first();
      log.debug("Limit Order queue not empty, so trading with best limit order: " + limitOrder.toString());
      limitOrders.remove(limitOrder);
      makeTrade(marketOrder, limitOrder, limitOrder.getLimit(), tickerData);
    }
  }

  private void makeTrade(ReadyOrder a, ReadyOrder b, float limit, TickerData ticketData) {
    ticketData.setLastExecutedTradePrice(limit);
    if(a.getDirection().equals(DIRECTION.BUY)) {
      Trade trade = Trade.builder()
          .buyOrder(a.getOrderId())
          .sellOrder(b.getOrderId())
          .matchQuantity(a.getQuantity())
          .matchPrice(limit)
          .build();
      log.debug("Making Buy trade: " + trade.toString());
      trades.add(trade);
    } else if(a.getDirection().equals(DIRECTION.SELL)) {
      Trade trade = Trade.builder()
          .buyOrder(b.getOrderId())
          .sellOrder(a.getOrderId())
          .matchQuantity(a.getQuantity())
          .matchPrice(limit)
          .build();
      log.debug("Making Sell trade: " + trade.toString());
      trades.add(trade);
    } else {
      throw new UnsupportedOperationException("Order direction not supported");
    }
  }

  private void processLimitOrder(LimitOrder limitOrder) {
    TickerData tickerData = getTickerQueueGroup(limitOrder);
    if (limitOrder.getDirection() == DIRECTION.BUY) {
      processDirectedLimitOrder(limitOrder, tickerData,
          tickerData.getSellMarketOrders(),
          tickerData.getBuyLimitOrders(),
          tickerData.getSellLimitOrders());
    } else if (limitOrder.getDirection() == DIRECTION.SELL) {
      processDirectedLimitOrder(limitOrder, tickerData,
          tickerData.getBuyMarketOrders(),
          tickerData.getSellLimitOrders(),
          tickerData.getBuyLimitOrders());
    } else {
      throw new UnsupportedOperationException("Order direction not supported");
    }
  }

  private void processDirectedLimitOrder(LimitOrder limitOrder, TickerData tickerData,
      SortedSet<MarketOrder> marketOrders,
      SortedSet<LimitOrder> sameTypeLimitOrders,
      SortedSet<LimitOrder> oppositeTypeLimitOrders) {
    log.debug("Checking main.Market Order queue");
    if(marketOrders.isEmpty()) {
      log.debug("main.Market Order queue empty, so checking Limit orders");
      if(oppositeTypeLimitOrders.isEmpty()) {
        log.debug("Limit Order queue empty, so check if time in force");
        queueIfTimeInForce(limitOrder, sameTypeLimitOrders);
      } else {
        LimitOrder otherLimitOrder = oppositeTypeLimitOrders.first();
        log.debug("Limit Order queue not empty, so checking if best order matches: " + otherLimitOrder.toString());

        if(limitOrder.limitMatches(otherLimitOrder)) {
          log.debug("Limits match so completing trade");
          oppositeTypeLimitOrders.remove(otherLimitOrder);
          makeTrade(limitOrder, otherLimitOrder, otherLimitOrder.getLimit(), tickerData);
        } else {
          log.debug("Limits do not match, so check if time in force");
          queueIfTimeInForce(limitOrder, sameTypeLimitOrders);
        }
      }
    } else {
      log.debug("main.Market Order queue not empty, so trading with oldest order: " + limitOrder.toString());
      MarketOrder marketOrder = marketOrders.first();
      marketOrders.remove(marketOrder);
      makeTrade(marketOrder, limitOrder, limitOrder.getLimit(), tickerData);
    }
  }

  public List<Trade> getAllMatchedTrades() {
    return trades;
  }


}
