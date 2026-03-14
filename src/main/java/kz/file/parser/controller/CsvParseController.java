package kz.file.parser.controller;

import kz.file.parser.model.CsvParseRequest;
import kz.file.parser.model.ParseResult;
import kz.file.parser.service.CsvParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/csv")
@RequiredArgsConstructor
public class CsvParseController {

  private final CsvParseService csvParseService;

  @PostMapping(path = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ParseResult> parse(
      @RequestParam("file") MultipartFile file,
      @RequestPart("meta") CsvParseRequest request
  ) throws IOException {
    var result = csvParseService.parseAndSend(file, request);
    return ResponseEntity.ok(result);
  }
}
