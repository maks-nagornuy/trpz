package com.example.demo.editor;

public class FileNode {
    private String name;     
    private String path;    
    private boolean dir;     
    private long size;       

    public FileNode(String name, String path, boolean dir, long size) {
        this.name = name;
        this.path = path;
        this.dir = dir;
        this.size = size;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public boolean isDir() { return dir; }
    public long getSize() { return size; }
}