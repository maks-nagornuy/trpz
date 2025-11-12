package com.example.demo.strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface StrategyArchive {
    void compress(List<File> files, String outputPath) throws IOException;
}