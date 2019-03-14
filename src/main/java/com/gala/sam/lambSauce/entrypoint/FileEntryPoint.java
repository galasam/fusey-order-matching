package com.gala.sam.lambSauce.entrypoint;


import com.gala.sam.lambSauce.domain.Order;
import com.gala.sam.lambSauce.domain.Trade;
import com.gala.sam.lambSauce.service.OrderMatchingService;
import com.gala.sam.lambSauce.utils.CSVParser;
import com.gala.sam.lambSauce.utils.FileIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FileEntryPoint {

  final OrderMatchingService orderMatchingService;

  private static final int phaseNumber = 4;
  private static final String relativeDirectoryOfTestFiles = "test truths/Phase" + Integer.toString(phaseNumber);
  private static final String absoluteDirectoryOfTestFiles = Paths
      .get(System.getProperty("user.dir"), relativeDirectoryOfTestFiles).toString();

  public void processTradesFromFile() throws IOException {
    log.info("Processing trades from file");
    int i = 1;
    while(testFileExists(i)) {
      runTest(i++);
    }
  }

  private boolean testFileExists(int testNumber) {
    String filepath = getInputFilePath(testNumber);
    return FileIO.fileExists(filepath);
  }

  private void runTest(int i) throws IOException {
    log.info(String.format("Running test %d", i));
    final List<Order> orders = readOrders(i);
    final List<Trade> trades = orderMatchingService.getResultingTrades(orders);
    writeTrades(i, trades);
  }

  private List<Order> readOrders(int testNumber) throws IOException {
    log.info("Reading Orders from file");
    String filepath = getInputFilePath(testNumber);
    final List<String> inputText = FileIO.readTestFile(filepath);
    return CSVParser.decodeCSV(inputText);
  }

  private String getInputFilePath(int testNumber) {
    final String filename = String.format("input.test%d.%d.csv", phaseNumber, testNumber);
    return Paths.get(absoluteDirectoryOfTestFiles, filename).toString();
  }

  private void writeTrades(int testNumber, List<Trade> trades)
      throws FileNotFoundException, UnsupportedEncodingException {
    log.info("Writing trades to file");
    final List<String> outputText = CSVParser.encodeCSV(trades);
    final String filename = String.format("output.test%d.%d.csv", phaseNumber, testNumber);
    final String filePath = Paths.get(absoluteDirectoryOfTestFiles, filename).toString();
    FileIO.writeTestFile(filePath, outputText);
  }

}
