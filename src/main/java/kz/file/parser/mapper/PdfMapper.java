package kz.file.parser.mapper;

import kz.file.parser.model.PdfTariffEvent;
import org.mapstruct.Mapper;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface PdfMapper {

  default PdfTariffEvent toTariffEvent(
      String[] columns,
      int dataColumns,
      String sourceFile
  ) {
    return new PdfTariffEvent(
        getColumn(columns, 0),
        getColumn(columns, 1),
        getColumn(columns, 2),
        getColumn(columns, 3),
        dataColumns > 4 ? getColumn(columns, 4) : null,
        getColumn(columns, columns.length - 2),
        getColumn(columns, columns.length - 1),
        sourceFile,
        Instant.now()
    );
  }

  private String getColumn(String[] columns, int index) {
    if (index < 0 || index >= columns.length) {
      return null;
    }
    String value = columns[index].trim();
    return value.isEmpty() ? null : value;
  }
}
