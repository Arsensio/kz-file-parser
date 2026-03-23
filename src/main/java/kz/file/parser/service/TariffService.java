package kz.file.parser.service;

import kz.file.parser.connector.AnalyticsAdminClient;
import kz.file.parser.model.TariffCsvRow;
import kz.file.parser.model.TariffEvent;
import kz.file.parser.model.TariffParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
@RequiredArgsConstructor
public class TariffService {

  private final AnalyticsAdminClient analyticsAdminClient;
  private final TariffPdfLineParser tariffPdfLineParser;
  private final TariffRabbitSender tariffRabbitSender;

  public TariffParseResult parsePdfAndSend(MultipartFile file, String authorization) throws IOException {
    if (authorization == null || authorization.isBlank()) {
      throw new ResponseStatusException(BAD_REQUEST, "Authorization header is required");
    }

    Integer deletedCount = analyticsAdminClient.deleteTariffs(authorization);
    List<TariffCsvRow> rows = parsePdf(file);
    int sent = sendRows(rows, file.getOriginalFilename(), "pdf");
    return new TariffParseResult(
        file.getOriginalFilename(),
        deletedCount == null ? 0 : deletedCount,
        rows.size(),
        sent);
  }

  private List<TariffCsvRow> parsePdf(MultipartFile file) throws IOException {
    validateFile(file, "PDF file is required");
    if (!hasExtension(file.getOriginalFilename(), ".pdf")) {
      throw new ResponseStatusException(BAD_REQUEST, "Only PDF files are supported");
    }

    List<TariffCsvRow> rows = new ArrayList<>();
    try (PDDocument document = Loader.loadPDF(file.getBytes())) {
      for (int page = 1; page <= document.getNumberOfPages(); page++) {
        rows.addAll(tariffPdfLineParser.parsePage(document, page));
      }
    }

    if (rows.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, "No tariff rows found in PDF");
    }

    log.info("Parsed tariff PDF file={}, rows={}", file.getOriginalFilename(), rows.size());
    return rows;
  }

  private int sendRows(List<TariffCsvRow> rows, String sourceFile, String sourceType) {
    int sent = 0;
    for (TariffCsvRow row : rows) {
      tariffRabbitSender.send(new TariffEvent(
          row.page(),
          row.categoryLevel1(),
          row.categoryLevel2(),
          row.categoryLevel3(),
          row.categoryLevel4(),
          row.categoryLevel5(),
          row.categoryPath(),
          row.commissionWithoutVat(),
          row.commissionWithVat(),
          sourceFile,
          sourceType,
          Instant.now()));
      sent++;
    }
    log.info("Sent tariff events sourceFile={}, sourceType={}, events={}", sourceFile, sourceType, sent);
    return sent;
  }

  private void validateFile(MultipartFile file, String message) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, message);
    }
  }

  private boolean hasExtension(String fileName, String extension) {
    return fileName != null && fileName.toLowerCase().endsWith(extension);
  }
}
