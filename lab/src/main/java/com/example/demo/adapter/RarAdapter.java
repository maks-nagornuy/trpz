package com.example.demo.adapter;
import com.example.demo.strategy.StrategyArchive;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RarAdapter implements StrategyArchive {

    private final String winrarPath;

    public RarAdapter(String winrarPath) {
        this.winrarPath = winrarPath;
    }

    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        StringBuilder cmd = new StringBuilder("\"" + winrarPath + "\" a -ep \"" + outputPath + "\"");

        for (File file : files) {
            cmd.append(" \"").append(file.getAbsolutePath()).append("\"");
        }

        Process process = Runtime.getRuntime().exec(cmd.toString());

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("WinRAR архівація завершилась з помилкою: " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException("Процес WinRAR перервано", e);
        }
    }
}
