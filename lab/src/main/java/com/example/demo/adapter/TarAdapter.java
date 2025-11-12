package com.example.demo.adapter;

import com.example.demo.strategy.StrategyArchive;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class TarAdapter implements StrategyArchive {

    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut)) {

            for (File file : files) {
                TarArchiveEntry entry = new TarArchiveEntry(file, file.getName());
                tarOut.putArchiveEntry(entry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.transferTo(tarOut);
                }

                tarOut.closeArchiveEntry();
            }

            tarOut.finish();
        }
    }
}
