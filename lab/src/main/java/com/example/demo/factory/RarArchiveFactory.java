package com.example.demo.factory;

import com.example.demo.strategy.StrategyArchive;
import com.example.demo.strategy.RarStrategyArchive;

public class RarArchiveFactory extends ArchiveFactory {

    public RarArchiveFactory() {
    }

    @Override
    public StrategyArchive createArchive() {
        return new RarStrategyArchive(); 
    }
}