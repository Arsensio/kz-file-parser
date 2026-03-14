package kz.file.parser.model;

public record ParseResult(
    String sourceFile,
    int linesSent
) {}
