package kz.file.parser.model;

import java.math.BigDecimal;

public record TariffCsvRow(
    Integer page,
    String categoryLevel1,
    String categoryLevel2,
    String categoryLevel3,
    String categoryLevel4,
    String categoryLevel5,
    String categoryPath,
    BigDecimal commissionWithoutVat,
    BigDecimal commissionWithVat,
    String rawLine
) {}
