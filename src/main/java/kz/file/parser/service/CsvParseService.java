package kz.file.parser.service;

import kz.file.parser.connector.AnalyticsAdminClient;
import kz.file.parser.mapper.CsvMapper;
import kz.file.parser.model.CsvParseRequest;
import kz.file.parser.model.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvParseService {

  private final AnalyticsAdminClient analyticsAdminClient;
  private final RabbitSender rabbitSender;
  private final CsvMapper csvMapper;

  public ParseResult parseAndSend(
      MultipartFile file,
      CsvParseRequest request,
      String authorization) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, "File is required");
    }
    if (request == null || request.year() == null || request.month() == null) {
      throw new ResponseStatusException(BAD_REQUEST, "Year and month are required");
    }
    if (authorization == null || authorization.isBlank()) {
      throw new ResponseStatusException(BAD_REQUEST, "Authorization header is required");
    }

    var sourceFile = file.getOriginalFilename();
    var sent = 0;

    analyticsAdminClient.deletePeriod(authorization, request.year(), request.month());

    log.info("Starting CSV parse for file={}", sourceFile);
    try (InputStreamReader reader = new InputStreamReader(
        file.getInputStream(), StandardCharsets.UTF_8);
         CSVParser parser = CSVFormat.DEFAULT.builder()
             .setHeader()
             .setSkipHeaderRecord(true)
             .setIgnoreHeaderCase(true)
             .setTrim(true)
             .build()
             .parse(reader)) {
      for (CSVRecord record : parser) {
        if (record.size() == 0) {
          continue;
        }
        var event = csvMapper.toCsvAnalysisEvent(request, record, sourceFile);
        rabbitSender.send(event);
        sent++;
      }
    }

    log.info("Finished CSV parse for file={}, eventsSent={}", sourceFile, sent);
    return new ParseResult(file.getOriginalFilename(), sent);
  }
}
