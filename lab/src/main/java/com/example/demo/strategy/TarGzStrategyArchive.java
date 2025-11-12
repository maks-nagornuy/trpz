package com.example.demo.strategy;

import com.example.demo.adapter.TarAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TarGzStrategyArchive implements StrategyArchive {

    private final TarAdapter tarAdapter;

    public TarGzStrategyArchive() {
        this.tarAdapter = new TarAdapter();
    }

    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        tarAdapter.compress(files, outputPath);
    }
}