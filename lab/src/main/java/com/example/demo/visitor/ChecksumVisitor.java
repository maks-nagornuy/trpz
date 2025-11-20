package com.example.demo.visitor;
import com.example.demo.model.Archive;
import com.example.demo.model.Meta;
import com.example.demo.model.BreakArchive;
import java.security.MessageDigest;
public class ChecksumVisitor implements ArchiveVisitor {
    private final StringBuilder data = new StringBuilder();
    @Override
    public void visit(Archive archive) {
        data.append(archive.getName());
        data.append(archive.getArchiveTypeId());
        data.append(archive.getUserId());
    }
    @Override
    public void visit(Meta meta) {
        data.append(meta.getFileName());
        data.append(meta.getSize());
    }
    @Override
    public void visit(BreakArchive part) {
        data.append(part.getName());
        data.append(part.getSize());
    }
    public String getChecksum() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.toString().getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "error";
        }
    }
}
