package com.example.demo.strategy;

import com.example.demo.adapter.ZipAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ZipStrategyArchive implements StrategyArchive {

    private final ZipAdapter zipAdapter;

    public ZipStrategyArchive() {
        this.zipAdapter = new ZipAdapter();
    }

    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        zipAdapter.compress(files, outputPath);
    }
}
