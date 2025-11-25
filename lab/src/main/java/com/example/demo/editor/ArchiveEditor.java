package com.example.demo.editor;

import java.io.File;
import java.io.IOException;

public interface ArchiveEditor {

    void deleteFile(File archiveFile, String entryName) throws IOException;
    void addFile(File archiveFile, File newFile, String entryName) throws IOException;
    void createFolder(File archiveFile, String folderPath) throws IOException;
    void deleteFolder(File archiveFile, String folderPathInArchive) throws IOException;
}