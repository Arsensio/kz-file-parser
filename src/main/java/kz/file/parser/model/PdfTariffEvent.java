package kz.file.parser.model;

import java.time.Instant;

public record PdfTariffEvent(
    String categoryLevel1,
    String categoryLevel2,
    String categoryLevel3,
    String categoryLevel4,
    String categoryLevel5,
    String commissionWithoutVat,
    String commissionWithVat,
    String sourceFile,
    Instant emittedAt
) {}
