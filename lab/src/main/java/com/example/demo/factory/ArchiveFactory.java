package com.example.demo.factory;

import com.example.demo.strategy.StrategyArchive;

public abstract class ArchiveFactory {
    public abstract StrategyArchive createArchive();
}