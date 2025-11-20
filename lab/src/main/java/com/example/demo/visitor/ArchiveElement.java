package com.example.demo.visitor;

public interface ArchiveElement {
    void accept(ArchiveVisitor visitor);
}
