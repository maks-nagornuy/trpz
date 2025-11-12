package com.example.demo.factory;

import com.example.demo.strategy.StrategyArchive;
import com.example.demo.strategy.ZipStrategyArchive;

public class ZipArchiveFactory extends ArchiveFactory {
    @Override
    public StrategyArchive createArchive() {
        return new ZipStrategyArchive();
    }
}