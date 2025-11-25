package com.example.demo.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RarArchiveEditor implements ArchiveEditor {

    private static final String RAR_EXE = "C:\\Program Files\\WinRAR\\Rar.exe";

    public RarArchiveEditor(String ignored) {
    }

    @Override
    public void addFile(File archiveFile, File newFile, String entryName) throws IOException {
        
        String internalPath = "";
        if (entryName.contains("/")) {
            internalPath = entryName.substring(0, entryName.lastIndexOf("/"));
            internalPath = internalPath.replace("/", "\\");
        }

        ProcessBuilder pb;
        if (!internalPath.isEmpty()) {
            pb = new ProcessBuilder(
                    RAR_EXE,
                    "a",           
                    "-ep",         
                    "-ap" + internalPath, 
                    archiveFile.getAbsolutePath(),
                    newFile.getAbsolutePath()
            );
        } else {
            pb = new ProcessBuilder(
                    RAR_EXE,
                    "a",
                    "-ep",
                    archiveFile.getAbsolutePath(),
                    newFile.getAbsolutePath()
            );
        }
        runProcess(pb);
    }
    @Override
    public void deleteFile(File archiveFile, String entryName) throws IOException {
        String pathToDelete = entryName.replace("/", "\\");

        ProcessBuilder pb = new ProcessBuilder(
                RAR_EXE,
                "d",            
                archiveFile.getAbsolutePath(),
                pathToDelete
        );
        runProcess(pb);
    }

    @Override
    public void createFolder(File archiveFile, String folderPath) throws IOException {
        File tempRoot = new File(System.getProperty("java.io.tmpdir"), "rar_folder_" + System.nanoTime());
        tempRoot.mkdirs();

        File marker = new File(tempRoot, ".keep");
        marker.createNewFile();

        String internalPath = folderPath.replace("/", "\\");
        if (internalPath.endsWith("\\")) internalPath = internalPath.substring(0, internalPath.length() - 1);

        ProcessBuilder pb = new ProcessBuilder(
                RAR_EXE,
                "a",
                "-ep1", 
                "-ap" + internalPath, 
                archiveFile.getAbsolutePath(),
                marker.getAbsolutePath() 
        );
        try {
            runProcess(pb);
        } finally {
            marker.delete();
            tempRoot.delete();
        }
        
    }


    @Override
    public void deleteFolder(File archiveFile, String folderPathInArchive) throws IOException {
        String pathToDelete = folderPathInArchive.replace("/", "\\");
        if (!pathToDelete.endsWith("\\")) pathToDelete += "\\"; 

        ProcessBuilder pb = new ProcessBuilder(
                RAR_EXE,
                "d",
                "-r", 
                archiveFile.getAbsolutePath(),
                pathToDelete
        );
        runProcess(pb);
    }

    private void runProcess(ProcessBuilder pb) throws IOException {
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
                throw new IOException("Rar.exe error code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted", e);
        }
    }
}