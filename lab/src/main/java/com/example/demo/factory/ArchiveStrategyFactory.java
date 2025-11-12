package com.example.demo.factory;

import com.example.demo.strategy.*;

public class ArchiveStrategyFactory {

    public static StrategyArchive create(String type) {
        return switch (type.toLowerCase()) {
            case "zip" -> new ZipStrategyArchive();
            case "tar.gz" -> new TarGzStrategyArchive();
            case "rar" -> new RarStrategyArchive("C:\\Program Files\\WinRAR\\WinRAR.exe");
            default -> throw new IllegalArgumentException("Невідомий тип архіву: " + type);
        };
    }
}