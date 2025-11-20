package com.example.demo.visitor;
import com.example.demo.model.Archive;
import com.example.demo.model.Meta;
import com.example.demo.model.BreakArchive;
public interface ArchiveVisitor {
    void visit(Archive archive);
    void visit(Meta meta);
    void visit(BreakArchive part);
}