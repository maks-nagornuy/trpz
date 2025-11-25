package com.example.demo.adapter;

import com.example.demo.strategy.StrategyArchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RarAdapter implements StrategyArchive {
    private static final String RAR_EXE = "C:\\Program Files\\WinRAR\\Rar.exe";
    @Override
    public void compress(List<File> files, String outputPath) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(RAR_EXE);
        command.add("a");
        command.add("-ep"); 
        command.add("-y");  
        command.add(outputPath);
        for (File file : files) {
            command.add(file.getAbsolutePath());
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); 
        Process p = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"))) {
            String line;
            while ((line = br.readLine()) != null) {
            }
        }
        try {
            int exitCode = p.waitFor();
            if (exitCode > 1) { 
                throw new IOException("Rar creation failed with code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Rar process interrupted", e);
        }
    }
}