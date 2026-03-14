package kz.file.parser.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import kz.file.parser.model.CsvAnalysisEvent;
import kz.file.parser.model.CsvParseRequest;
import org.apache.commons.csv.CSVRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CsvMapper {

  default CsvAnalysisEvent toCsvAnalysisEvent(
      CsvParseRequest request,
      CSVRecord record,
      String sourceFile) {
    return new CsvAnalysisEvent(
        getColumn(record, 0),
        getColumn(record, 1),
        getColumn(record, 2),
        getColumn(record, 3),
        getColumn(record, 4),
        getColumn(record, 5),
        parseInteger(getColumn(record, 6)),
        parseDecimal(getColumn(record, 7)),
        parseDecimal(getColumn(record, 8)),
        parseInteger(getColumn(record, 9)),
        request.year(),
        request.month(),
        sourceFile,
        Instant.now());
  }

  private String getColumn(CSVRecord record, int index) {
    if (index < 0 || index >= record.size()) {
      return null;
    }
    String value = record.get(index);
    if (value == null) {
      return null;
    }
    value = value.trim();
    return value.isEmpty() ? null : value;
  }

  private BigDecimal parseDecimal(String value) {
    if (value == null || value.isBlank() || "-".equals(value)) {
      return null;
    }
    String normalized = value.replace("\u00A0", "").replace(" ", "");
    return new BigDecimal(normalized);
  }

  private Integer parseInteger(String value) {
    if (value == null || value.isBlank() || "-".equals(value)) {
      return null;
    }
    String normalized = value.replace("\u00A0", "").replace(" ", "");
    return Integer.valueOf(normalized);
  }
}
