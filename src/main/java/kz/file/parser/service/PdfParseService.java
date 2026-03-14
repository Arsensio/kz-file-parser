package kz.file.parser.service;

import kz.file.parser.mapper.PdfMapper;
import kz.file.parser.model.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfParseService {

  private final RabbitSender rabbitSender;
  private final PdfMapper pdfMapper;

  public ParseResult process(MultipartFile file) throws IOException {
    String sourceFile = file.getOriginalFilename();
    int sent = 0;
    boolean headerSkipped = false;

    log.info("Starting PDF parse for file={}", sourceFile);
    try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
      PDFTextStripper textStripper = new PDFTextStripper();
      textStripper.setSortByPosition(true);
      textStripper.setWordSeparator("\t");
      String text = textStripper.getText(document);
      String[] lines = text.split("\\R");

      for (String rawLine : lines) {
        if (rawLine.trim().isEmpty()) {
          continue;
        }
        if (!headerSkipped) {
          headerSkipped = true;
          continue;
        }
        String[] columns = rawLine.split("\t", -1);
        int dataColumns = Math.max(columns.length - 2, 0);
        var tariffEvent = pdfMapper.toTariffEvent(columns, dataColumns, sourceFile);
        rabbitSender.send(tariffEvent);
        sent++;
      }
    }

    log.info("Finished PDF parse for file={}, eventsSent={}", sourceFile, sent);
    return new ParseResult(file.getOriginalFilename(), sent);
  }

}
