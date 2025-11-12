package com.example.demo.factory;

import com.example.demo.strategy.StrategyArchive;
import com.example.demo.strategy.TarGzStrategyArchive;

public class TarArchiveFactory extends ArchiveFactory {
    @Override
    public StrategyArchive createArchive() {
        return new TarGzStrategyArchive();
    }
}