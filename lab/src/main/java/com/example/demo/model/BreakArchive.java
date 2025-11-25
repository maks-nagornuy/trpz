package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.visitor.ArchiveElement;
import com.example.demo.visitor.ArchiveVisitor;

@Document(collection = "broken_archives")
public class BreakArchive implements ArchiveElement {

    @Id
    private String id;
    private String name;
    private long size;
    private String archiveId;

    public BreakArchive() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public void setArchiveId(String archiveId) {
        this.archiveId = archiveId;
    }
}