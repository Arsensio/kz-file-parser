package kz.file.parser.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TariffEvent(
    Integer page,
    String categoryLevel1,
    String categoryLevel2,
    String categoryLevel3,
    String categoryLevel4,
    String categoryLevel5,
    String categoryPath,
    BigDecimal commissionWithoutVat,
    BigDecimal commissionWithVat,
    String sourceFile,
    String sourceType,
    Instant emittedAt
) {}
