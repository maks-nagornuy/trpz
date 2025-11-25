package com.example.demo.factory;

import com.example.demo.adapter.RarAdapter;
import com.example.demo.strategy.StrategyArchive;
import com.example.demo.strategy.TarGzStrategyArchive;
import com.example.demo.strategy.ZipStrategyArchive;

public class ArchiveStrategyFactory {

    public static StrategyArchive create(String type) {
        return switch (type.toLowerCase()) {
            case "zip" -> new ZipStrategyArchive();
            case "tar.gz" -> new TarGzStrategyArchive();
            case "rar" -> new RarAdapter();
            default -> throw new IllegalArgumentException("Unknown archive type: " + type);
        };
    }
}