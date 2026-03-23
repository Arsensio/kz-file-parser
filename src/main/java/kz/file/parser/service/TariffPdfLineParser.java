package kz.file.parser.service;

import kz.file.parser.model.TariffCsvRow;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TariffPdfLineParser {

  private static final float ROW_TOLERANCE = 2.0f;
  private static final float CATEGORY_1_MAX_X = 110f;
  private static final float CATEGORY_2_MAX_X = 214f;
  private static final float CATEGORY_3_MAX_X = 433f;
  private static final float CATEGORY_4_MAX_X = 653f;
  private static final float CATEGORY_5_MAX_X = 960f;
  private static final float COMMISSION_WITHOUT_VAT_MAX_X = 1133f;

  public List<TariffCsvRow> parsePage(PDDocument document, int pageNumber) throws IOException {
    ColumnAwareStripper stripper = new ColumnAwareStripper();
    stripper.setSortByPosition(true);
    stripper.setStartPage(pageNumber);
    stripper.setEndPage(pageNumber);
    stripper.getText(document);
    return toLogicalRows(stripper.getRows(), pageNumber);
  }

  private List<TariffCsvRow> toLogicalRows(List<PhysicalRow> physicalRows, int pageNumber) {
    List<PhysicalRow> sortedRows = physicalRows.stream()
        .sorted(Comparator.comparingDouble(PhysicalRow::getY))
        .toList();

    List<TariffCsvRow> result = new ArrayList<>();
    PhysicalRow accumulator = new PhysicalRow(0f);
    boolean collecting = false;

    for (PhysicalRow row : sortedRows) {
      if (row.isHeader()) {
        continue;
      }

      if (row.isEmpty()) {
        continue;
      }

      accumulator.mergeCategoriesFrom(row);
      collecting = collecting || row.hasCategoryData();

      if (row.hasCommissions()) {
        accumulator.setCommissionWithoutVat(row.getCommissionWithoutVat());
        accumulator.setCommissionWithVat(row.getCommissionWithVat());
        if (collecting) {
          result.add(toTariffRow(accumulator, pageNumber));
        }
        accumulator = new PhysicalRow(0f);
        collecting = false;
      }
    }

    return result;
  }

  private TariffCsvRow toTariffRow(PhysicalRow row, int pageNumber) {
    String level1 = emptyToNull(row.getCategoryLevel1());
    String level2 = emptyToNull(row.getCategoryLevel2());
    String level3 = emptyToNull(row.getCategoryLevel3());
    String level4 = emptyToNull(row.getCategoryLevel4());
    String level5 = emptyToNull(row.getCategoryLevel5());

    return new TariffCsvRow(
        pageNumber,
        level3,
        level2,
        level1,
        level4,
        level5,
        joinPath(level1, level2, level3, level4, level5),
        parseRate(row.getCommissionWithoutVat()),
        parseRate(row.getCommissionWithVat()),
        String.join("\t", List.of(
            nullToEmpty(level1),
            nullToEmpty(level2),
            nullToEmpty(level3),
            nullToEmpty(level4),
            nullToEmpty(level5),
            nullToEmpty(row.getCommissionWithoutVat()),
            nullToEmpty(row.getCommissionWithVat()))));
  }

  private String joinPath(String... values) {
    List<String> parts = new ArrayList<>();
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        parts.add(value);
      }
    }
    return String.join(" ", parts);
  }

  private BigDecimal parseRate(String value) {
    return new BigDecimal(value.replace("%", "").replace(',', '.').trim());
  }

  private String emptyToNull(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private int resolveColumn(float x) {
    if (x < CATEGORY_1_MAX_X) {
      return 0;
    }
    if (x < CATEGORY_2_MAX_X) {
      return 1;
    }
    if (x < CATEGORY_3_MAX_X) {
      return 2;
    }
    if (x < CATEGORY_4_MAX_X) {
      return 3;
    }
    if (x < CATEGORY_5_MAX_X) {
      return 4;
    }
    if (x < COMMISSION_WITHOUT_VAT_MAX_X) {
      return 5;
    }
    return 6;
  }

  private final class ColumnAwareStripper extends PDFTextStripper {

    @Getter
    private final List<PhysicalRow> rows = new ArrayList<>();

    private ColumnAwareStripper() throws IOException {
      super();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) {
      if (textPositions == null || textPositions.isEmpty()) {
        return;
      }

      String normalized = normalizeText(text);
      if (normalized.isBlank()) {
        return;
      }

      float x = textPositions.get(0).getXDirAdj();
      float y = textPositions.get(0).getYDirAdj();
      int column = resolveColumn(x);
      PhysicalRow row = findOrCreateRow(y);
      row.append(column, normalized);
    }

    private PhysicalRow findOrCreateRow(float y) {
      for (int i = rows.size() - 1; i >= 0; i--) {
        PhysicalRow row = rows.get(i);
        if (Math.abs(row.getY() - y) <= ROW_TOLERANCE) {
          return row;
        }
      }
      PhysicalRow row = new PhysicalRow(y);
      rows.add(row);
      return row;
    }
  }

  private String normalizeText(String value) {
    return value.replace('\u00A0', ' ')
        .replaceAll("\\s+", " ")
        .trim();
  }

  private static final class PhysicalRow {

    @Getter
    private final float y;
    private final StringBuilder[] cells = {
        new StringBuilder(),
        new StringBuilder(),
        new StringBuilder(),
        new StringBuilder(),
        new StringBuilder(),
        new StringBuilder(),
        new StringBuilder()
    };

    private PhysicalRow(float y) {
      this.y = y;
    }

    private void append(int column, String value) {
      if (column < 0 || column >= cells.length || value == null || value.isBlank()) {
        return;
      }

      StringBuilder cell = cells[column];
      if (!cell.isEmpty()) {
        cell.append(' ');
      }
      cell.append(value.trim());
    }

    private void mergeCategoriesFrom(PhysicalRow row) {
      for (int i = 0; i < 5; i++) {
        append(i, row.getCell(i));
      }
    }

    private void setCommissionWithoutVat(String value) {
      cells[5].setLength(0);
      cells[5].append(value);
    }

    private void setCommissionWithVat(String value) {
      cells[6].setLength(0);
      cells[6].append(value);
    }

    private boolean isHeader() {
      return getCategoryLevel1().startsWith("Категория 1-го уровня")
          || getCommissionWithoutVat().contains("Комиссия без НДС");
    }

    private boolean hasCategoryData() {
      return !getCategoryLevel1().isBlank()
          || !getCategoryLevel2().isBlank()
          || !getCategoryLevel3().isBlank()
          || !getCategoryLevel4().isBlank()
          || !getCategoryLevel5().isBlank();
    }

    private boolean hasCommissions() {
      return !getCommissionWithoutVat().isBlank() && !getCommissionWithVat().isBlank();
    }

    private boolean isEmpty() {
      for (StringBuilder cell : cells) {
        if (!cell.isEmpty()) {
          return false;
        }
      }
      return true;
    }

    private String getCategoryLevel1() {
      return getCell(0);
    }

    private String getCategoryLevel2() {
      return getCell(1);
    }

    private String getCategoryLevel3() {
      return getCell(2);
    }

    private String getCategoryLevel4() {
      return getCell(3);
    }

    private String getCategoryLevel5() {
      return getCell(4);
    }

    private String getCommissionWithoutVat() {
      return getCell(5);
    }

    private String getCommissionWithVat() {
      return getCell(6);
    }

    private String getCell(int index) {
      return cells[index].toString().trim();
    }
  }
}
