package kz.file.parser.controller;

import java.io.IOException;

import kz.file.parser.model.ParseResult;
import kz.file.parser.service.PdfParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfParseController {

  private final PdfParseService pdfParseService;

  @PostMapping(path = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ParseResult> parse(@RequestParam("file") MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(BAD_REQUEST, "File is required");
    }

    var result = pdfParseService.process(file);
    return ResponseEntity.ok(result);
  }
}
