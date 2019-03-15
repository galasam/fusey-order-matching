package com.gala.sam.lambSauce.entrypoint;

import com.gala.sam.lambSauce.domain.Order;
import com.gala.sam.lambSauce.domain.Trade;
import com.gala.sam.lambSauce.service.OrderMatchingService;
import com.gala.sam.lambSauce.utils.CSVParser;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class RestEntryPoint {

  final OrderMatchingService orderMatchingService;

  @PostMapping("/")
  public String processTrades(@RequestBody String csvInput) {
    final Stream<String> inputRows = Pattern.compile("\n").splitAsStream(csvInput);
    final List<Order> orders = CSVParser.decodeCSV(inputRows);
    final List<Trade> trades = orderMatchingService.getResultingTrades(orders);
    final String csvOuput = String.join("\n", CSVParser.encodeCSV(trades));
    return csvOuput;
  }

}
