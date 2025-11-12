package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.strategy.StrategyArchive;
import com.example.demo.factory.ArchiveStrategyFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ArchiveService {

    public void createArchive(List<File> files, String outputPath, String type) throws IOException {
        StrategyArchive strategy = ArchiveStrategyFactory.create(type);
        strategy.compress(files, outputPath);
    }
}