package com.example.demo.editor;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.*;

import java.io.*;

public class TarGzArchiveEditor implements ArchiveEditor {

    @Override
    public void deleteFile(File archiveFile, String entryName) throws IOException {
        rewriteTarGz(archiveFile, null, null, entryName, null, false);
    }

    @Override
    public void addFile(File archiveFile, File newFile, String entryName) throws IOException {
        rewriteTarGz(archiveFile, newFile, entryName, null, null, false);
    }

    @Override
    public void createFolder(File archiveFile, String folderPath) throws IOException {
        if (!folderPath.endsWith("/")) folderPath += "/";
        rewriteTarGz(archiveFile, null, null, null, folderPath, true);
    }


    @Override
    public void deleteFolder(File archiveFile, String folderPathInArchive) throws IOException {
        if (!folderPathInArchive.endsWith("/")) folderPathInArchive += "/";
        rewriteTarGz(archiveFile, null, null, null, folderPathInArchive, false);
    }

    private void rewriteTarGz(File archiveFile,
                              File fileToAdd,
                              String addAsName,
                              String fileToDelete,
                              String folderPathOp,
                              boolean createFolderFlag) throws IOException {

        File tempFile = File.createTempFile("tar_edit_", ".tar.gz");

        boolean hasOld = archiveFile.exists() && archiveFile.length() > 0;

        try (TarArchiveInputStream tis = hasOld
                        ? new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(archiveFile)))
                        : null;
             TarArchiveOutputStream tos =
                     new TarArchiveOutputStream(new GzipCompressorOutputStream(new FileOutputStream(tempFile)))) {

            if (tis != null) {
                TarArchiveEntry entry;
                while ((entry = tis.getNextTarEntry()) != null) {
                    String name = entry.getName();

                    if (folderPathOp != null && !createFolderFlag && name.startsWith(folderPathOp)) {
                        continue;
                    }

                    if (fileToDelete != null && name.equals(fileToDelete)) {
                        continue;
                    }

                    if (fileToAdd != null && name.equals(addAsName)) {
                        continue;
                    }

                    TarArchiveEntry newEntry = new TarArchiveEntry(name);
                    newEntry.setSize(entry.getSize());
                    newEntry.setMode(entry.getMode());
                    tos.putArchiveEntry(newEntry);

                    if (!entry.isDirectory()) {
                        tis.transferTo(tos);
                    }
                    tos.closeArchiveEntry();
                }
            }

            if (createFolderFlag && folderPathOp != null) {
                TarArchiveEntry dirEntry = new TarArchiveEntry(folderPathOp);
                dirEntry.setMode(0755);
                dirEntry.setSize(0);
                tos.putArchiveEntry(dirEntry);
                tos.closeArchiveEntry();
            }

            if (fileToAdd != null) {
                TarArchiveEntry newEntry = new TarArchiveEntry(addAsName);
                newEntry.setSize(fileToAdd.length());
                tos.putArchiveEntry(newEntry);

                try (InputStream fis = new FileInputStream(fileToAdd)) {
                    fis.transferTo(tos);
                }
                tos.closeArchiveEntry();
            }

            tos.finish();
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