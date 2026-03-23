package kz.file.parser.controller;

import kz.file.parser.model.TariffParseResult;
import kz.file.parser.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/tariffs")
@RequiredArgsConstructor
public class TariffController {

  private final TariffService tariffService;

  @PostMapping(path = "/pdf/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<TariffParseResult> parsePdf(
      @RequestParam("file") MultipartFile file,
      @RequestHeader("Authorization") String authorization)
      throws IOException {
    return ResponseEntity.ok(tariffService.parsePdfAndSend(file, authorization));
  }
}
