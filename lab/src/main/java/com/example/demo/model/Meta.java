package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.visitor.ArchiveElement;
import com.example.demo.visitor.ArchiveVisitor;

@Document(collection = "meta")
public class Meta implements ArchiveElement {

    @Id
    private String id;
    private String fileName;
    private long size;

    public Meta() {}

    @Override
    public void accept(ArchiveVisitor visitor) {
        visitor.visit(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}