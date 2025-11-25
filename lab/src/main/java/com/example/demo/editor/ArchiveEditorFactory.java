package com.example.demo.editor;

public class ArchiveEditorFactory {
    public static ArchiveEditor create(String type) {
        return switch (type.toLowerCase()) {
            case "zip" -> new ZipArchiveEditor();
            case "tar.gz" -> new TarGzArchiveEditor();
            case "rar" -> new RarArchiveEditor("ignored");
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}