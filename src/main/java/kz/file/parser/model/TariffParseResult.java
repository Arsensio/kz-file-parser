package kz.file.parser.model;

public record TariffParseResult(
    String sourceFile,
    int deletedCount,
    int rowsParsed,
    int eventsSent
) {}
