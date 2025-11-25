package com.example.demo.adapter;

import com.example.demo.strategy.StrategyArchive;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ZipAdapter implements StrategyArchive {
    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(outputPath))) {

            for (File file : files) {
                ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
                zipOut.putArchiveEntry(entry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.transferTo(zipOut);
                }

                zipOut.closeArchiveEntry();
            }
        }
    }
}