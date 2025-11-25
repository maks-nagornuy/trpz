package com.example.demo.strategy;

import com.example.demo.adapter.RarAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RarStrategyArchive implements StrategyArchive {

    private final RarAdapter rarAdapter;

    public RarStrategyArchive() {
        this.rarAdapter = new RarAdapter(); 
    }

    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        rarAdapter.compress(files, outputPath);
    }
}