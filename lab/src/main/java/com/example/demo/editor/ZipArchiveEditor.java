package com.example.demo.editor;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;

public class ZipArchiveEditor implements ArchiveEditor {

    @Override
    public void deleteFile(File archiveFile, String entryName) throws IOException {
        rewriteZip(archiveFile, null, null, entryName, null, false);
    }

    @Override
    public void addFile(File archiveFile, File newFile, String entryName) throws IOException {
        rewriteZip(archiveFile, newFile, entryName, null, null, false);
    }

    @Override
    public void createFolder(File archiveFile, String folderPath) throws IOException {
        if (!folderPath.endsWith("/")) folderPath += "/";
        rewriteZip(archiveFile, null, null, null, folderPath, true);
    }



    private void addFolderRecursive(File archiveFile, File baseFolder, String parentInArchive, String currentName) throws IOException {
        File current = new File(baseFolder, currentName);
        String currentArchivePath = parentInArchive + currentName;

        if (current.isDirectory()) {
            createFolder(archiveFile, currentArchivePath + "/");
            File[] children = current.listFiles();
            if (children != null) {
                for (File ch : children) {
                    addFolderRecursive(archiveFile, current, "", ch.getName());
                }
            }
        } else {
            addFile(archiveFile, current, currentArchivePath);
        }
    }

    @Override
    public void deleteFolder(File archiveFile, String folderPathInArchive) throws IOException {
        if (!folderPathInArchive.endsWith("/")) folderPathInArchive += "/";
        rewriteZip(archiveFile, null, null, null, folderPathInArchive, false);
    }

    private void rewriteZip(File archiveFile,
                            File fileToAdd,
                            String addAsName,
                            String fileToDelete,
                            String folderPathOp,
                            boolean createFolderFlag) throws IOException {

        File tempFile = File.createTempFile("zip_edit_", ".zip");

        try (ZipFile zipFile = archiveFile.exists() ? new ZipFile(archiveFile) : null;
             ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(tempFile))) {

            if (zipFile != null) {
                Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry e = entries.nextElement();
                    String name = e.getName();

                    if (folderPathOp != null && !createFolderFlag && name.startsWith(folderPathOp)) {
                        continue;
                    }

                    if (fileToDelete != null && name.equals(fileToDelete)) {
                        continue;
                    }

                    if (fileToAdd != null && name.equals(addAsName)) {
                        continue;
                    }

                    ZipArchiveEntry newEntry = new ZipArchiveEntry(name);
                    zipOut.putArchiveEntry(newEntry);

                    try (InputStream is = zipFile.getInputStream(e)) {
                        is.transferTo(zipOut);
                    }

                    zipOut.closeArchiveEntry();
                }
            }

            if (createFolderFlag && folderPathOp != null) {
                ZipArchiveEntry dirEntry = new ZipArchiveEntry(folderPathOp);
                zipOut.putArchiveEntry(dirEntry);
                zipOut.closeArchiveEntry();
            }

            if (fileToAdd != null) {
                ZipArchiveEntry newEntry = new ZipArchiveEntry(addAsName);
                zipOut.putArchiveEntry(newEntry);

                try (InputStream fis = new FileInputStream(fileToAdd)) {
                    fis.transferTo(zipOut);
                }

                zipOut.closeArchiveEntry();
            }
        }

        if (archiveFile.exists()) archiveFile.delete();
        if (!tempFile.renameTo(archiveFile)) {
            try (InputStream in = new FileInputStream(tempFile);
                 OutputStream out = new FileOutputStream(archiveFile)) {
                in.transferTo(out);
            }
            tempFile.delete();
        }
    }
}