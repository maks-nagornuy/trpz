package com.example.demo.factory;

import com.example.demo.strategy.StrategyArchive;
import com.example.demo.strategy.RarStrategyArchive;

public class RarArchiveFactory extends ArchiveFactory {
    private final String winrarPath;

    public RarArchiveFactory(String winrarPath) {
        this.winrarPath = winrarPath;
    }

    @Override
    public StrategyArchive createArchive() {
        return new RarStrategyArchive(winrarPath);
    }
}