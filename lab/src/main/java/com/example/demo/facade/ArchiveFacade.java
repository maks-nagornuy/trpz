package com.example.demo.facade;
import com.example.demo.model.Archive;
import com.example.demo.model.Meta;
import com.example.demo.repository.ArchiveRepository;
import com.example.demo.service.ArchiveService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
@Component
public class ArchiveFacade {
    private final ArchiveService archiveService;
    private final ArchiveRepository archiveRepository;
    public ArchiveFacade(ArchiveService archiveService, ArchiveRepository archiveRepository) {
        this.archiveService = archiveService;
        this.archiveRepository = archiveRepository;
    }
    public void createArchive(Archive archive, MultipartFile[] files) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        Set<Meta> metaSet = new HashSet<>();
        for (MultipartFile multipart : files) {
            if (!multipart.isEmpty()) {
                File temp = File.createTempFile("upload_", "_" + multipart.getOriginalFilename());
                multipart.transferTo(temp);
                tempFiles.add(temp);

                Meta meta = new Meta();
                meta.setFileName(multipart.getOriginalFilename());
                meta.setSize(multipart.getSize());
                metaSet.add(meta);
            }
        }
        archive.setMeta(metaSet);
        archive.setChecksum(generateChecksum(archive));
        String outputDir = "C:/archives/";
        new File(outputDir).mkdirs();
        String outputPath = outputDir + archive.getName() + "." + archive.getArchiveTypeId();
        try {
            archiveService.createArchive(tempFiles, outputPath, archive.getArchiveTypeId());
        } finally {
            for (File f : tempFiles) f.delete();
        }
        archive.setBreakArchiveIds(Set.of("part1", "part2"));
        archiveRepository.save(archive);
    }
    private String generateChecksum(Archive arch) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = arch.getName() + arch.getArchiveTypeId() + System.currentTimeMillis();
            byte[] hash = digest.digest(data.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();

        } catch (Exception e) {
            return "error";
        }
    }
    public List<Archive> getAllArchives() {
        return archiveRepository.findAll();
    }
}
