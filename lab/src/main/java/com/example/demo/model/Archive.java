package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.visitor.ArchiveElement;
import com.example.demo.visitor.ArchiveVisitor;

import java.util.Set;

@Document(collection = "archives")
public class Archive implements ArchiveElement {

    @Id
    private String id;
    private String name;
    private String checksum;
    private String archiveTypeId;
    private String userId;
    private Set<Meta> meta;
    private Set<String> breakArchiveIds;

    public Archive() {}

    @Override
    public void accept(ArchiveVisitor visitor) {
        visitor.visit(this);

        if (meta != null) {
            for (Meta m : meta) {
                m.accept(visitor);
            }
        }
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

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getArchiveTypeId() {
        return archiveTypeId;
    }

    public void setArchiveTypeId(String archiveTypeId) {
        this.archiveTypeId = archiveTypeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<Meta> getMeta() {
        return meta;
    }

    public void setMeta(Set<Meta> meta) {
        this.meta = meta;
    }

    public Set<String> getBreakArchiveIds() {
        return breakArchiveIds;
    }

    public void setBreakArchiveIds(Set<String> breakArchiveIds) {
        this.breakArchiveIds = breakArchiveIds;
    }
}