package com.gala.sam.lambSauce.utils;

import com.gala.sam.lambSauce.domain.LimitOrder;
import com.gala.sam.lambSauce.domain.MarketOrder;
import com.gala.sam.lambSauce.domain.Order;
import com.gala.sam.lambSauce.domain.ReadyOrder.DIRECTION;
import com.gala.sam.lambSauce.domain.ReadyOrder.TIME_IN_FORCE;
import com.gala.sam.lambSauce.domain.StopOrder;
import com.gala.sam.lambSauce.domain.Trade;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVParser {

  private final static Map<String, Integer> INPUT_HEADINGS = new TreeMap<>();
  static {
    INPUT_HEADINGS.put("ORDER ID", 0);
    INPUT_HEADINGS.put("GROUP ID", 1);
    INPUT_HEADINGS.put("DIRECTION", 2);
    INPUT_HEADINGS.put("QUANTITY", 3);
    INPUT_HEADINGS.put("TICKER", 4);
    INPUT_HEADINGS.put("TYPE", 5);
    INPUT_HEADINGS.put("LIMIT PRICE", 6);
    INPUT_HEADINGS.put("TIME IN FORCE", 7);
    INPUT_HEADINGS.put("TRIGGER PRICE", 8);
  }
  private final static String OUTPUT_HEADER = String.join(",", "BUY ORDER", "SELL ORDER", "MATCH QTY", "MATCH PRICE");

  public static List<Order> decodeCSV(List<String> input) {
    return input.stream()
        .skip(1)
        .map(CSVParser::decodeCSVRow)
        .collect(Collectors.toList());
  }

  private static Order decodeCSVRow(String input) {
    final String[] values = input.split(",");

    final int orderId = Integer.parseInt(values[INPUT_HEADINGS.get("ORDER ID")]);
    final DIRECTION direction = DIRECTION.valueOf(values[INPUT_HEADINGS.get("DIRECTION")]);
    final int quantity = Integer.parseInt(values[INPUT_HEADINGS.get("QUANTITY")]);
    final String type = values[INPUT_HEADINGS.get("TYPE")];
    final TIME_IN_FORCE tif = TIME_IN_FORCE.valueOf(values[INPUT_HEADINGS.get("TIME IN FORCE")]);
    final String ticker = values[INPUT_HEADINGS.get("TICKER")];

    switch (type) {
      case "LIMIT":
        float limit = Float.parseFloat(values[INPUT_HEADINGS.get("LIMIT PRICE")]);
        return LimitOrder.builder()
            .orderId(orderId)
            .direction(direction)
            .quantity(quantity)
            .timeInForce(tif)
            .ticker(ticker)
            .limit(limit)
            .build();
      case "MARKET":
        return MarketOrder.builder()
            .orderId(orderId)
            .direction(direction)
            .quantity(quantity)
            .timeInForce(tif)
            .ticker(ticker)
            .build();
      case "STOP-LIMIT":
        limit = Float.parseFloat(values[INPUT_HEADINGS.get("LIMIT PRICE")]);
        float triggerPrice = Float.parseFloat(values[INPUT_HEADINGS.get("TRIGGER PRICE")]);
        return StopOrder.builder()
            .readyOrder(LimitOrder.builder()
                .orderId(orderId)
                .direction(direction)
                .quantity(quantity)
                .timeInForce(tif)
                .ticker(ticker)
                .limit(limit)
                .build())
            .triggerPrice(triggerPrice)
            .build();
      case "STOP-MARKET":
        triggerPrice = Float.parseFloat(values[INPUT_HEADINGS.get("TRIGGER PRICE")]);
        return StopOrder.builder()
            .readyOrder(MarketOrder.builder()
                .orderId(orderId)
                .direction(direction)
                .quantity(quantity)
                .timeInForce(tif)
                .ticker(ticker)
                .build())
            .triggerPrice(triggerPrice)
            .build();

      default:
        throw new UnsupportedOperationException(" Unsupported order type");
    }
  }

  public static List<String> encodeCSV(List<Trade> output) {
    return Stream.concat(
        Stream.of(OUTPUT_HEADER),
        output.stream().map(CSVParser::encodeCSVRow)
    ).collect(Collectors.toList());
  }

  private static String encodeCSVRow(Trade output) {
    return String.join(",",
        Integer.toString(output.getBuyOrder()),
        Integer.toString(output.getSellOrder()),
        Integer.toString(output.getMatchQuantity()),
        Float.toString(output.getMatchPrice()));
  }



}
