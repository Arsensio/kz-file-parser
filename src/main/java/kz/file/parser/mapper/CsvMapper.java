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
        getColumn(record, "Бренд", "brand"),
        getColumn(record, "Артик", "Артикул", "article"),
        getColumn(record, "Товар", "product"),
        getColumn(record, "Категория3", "category3"),
        getColumn(record, "Категория2", "category2"),
        getColumn(record, "Категория", "category", "Категория1"),
        parseInteger(getColumn(record, "Заказы", "orders")),
        parseDecimal(getColumn(record, "Выручка", "revenue")),
        parseDecimal(getColumn(record, "Ср чек", "Ср. чек", "Ср_чек", "avgCheck")),
        parseInteger(getColumn(record, "Прод", "Продажи", "sold")),
        request.year(),
        request.month(),
        sourceFile,
        Instant.now());
  }

  private String getColumn(CSVRecord record, String... headers) {
    for (String header : headers) {
      if (record.isMapped(header)) {
        String value = record.get(header);
        if (value == null) {
          return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
      }
    }
    return null;
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
