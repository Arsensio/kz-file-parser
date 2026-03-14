package kz.file.parser.model;

import java.math.BigDecimal;
import java.time.Instant;

public record CsvAnalysisEvent(
    String brand,
    String article,
    String product,
    String category3,
    String category2,
    String category,
    Integer orders,
    BigDecimal revenue,
    BigDecimal avgCheck,
    Integer sold,
    Integer year,
    Integer month,
    String sourceFile,
    Instant emittedAt
) {}
