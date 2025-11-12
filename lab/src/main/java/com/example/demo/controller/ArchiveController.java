package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Archive;
import com.example.demo.model.Meta;
import com.example.demo.repository.ArchiveRepository;
import com.example.demo.service.ArchiveService;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveRepository archiveRepository;
    private final ArchiveService archiveService;

    public ArchiveController(ArchiveRepository archiveRepository, ArchiveService archiveService) {
        this.archiveRepository = archiveRepository;
        this.archiveService = archiveService;
    }

    @GetMapping("/archives")
    public String showArchives(Model model) {
        model.addAttribute("archives", archiveRepository.findAll());
        return "archives";
    }
    
    @GetMapping("/create")
    public String createArchiveForm(Model model) {
        model.addAttribute("archive", new Archive());
        model.addAttribute("archiveTypes", new String[]{"zip", "tar.gz", "rar"});
        return "create_archive";
    }

    @PostMapping("/create")
    public String createArchiveSubmit(
            @ModelAttribute("archive") Archive archive,
            @RequestParam("files") MultipartFile[] files) throws IOException {

        Set<Meta> metaSet = new HashSet<>();
        List<File> tempFiles = new ArrayList<>();

        // Збереження тимчасових файлів
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
                file.transferTo(tempFile);
                tempFiles.add(tempFile);

                // метаданні
                Meta meta = new Meta();
                meta.setFileName(file.getOriginalFilename());
                meta.setSize(file.getSize());
                metaSet.add(meta);
            }
        }

        archive.setMeta(metaSet);
        archive.setChecksum(generateChecksum(archive));

        // шлях до архіву
        String outputDir = "C:/archives/";
        new File(outputDir).mkdirs();
        String outputPath = outputDir + archive.getName() + "." + archive.getArchiveTypeId();

        try {
            archiveService.createArchive(tempFiles, outputPath, archive.getArchiveTypeId());
        } finally {
            for (File f : tempFiles) f.delete();
        }

        // умовні частини
        Set<String> parts = new HashSet<>();
        parts.add("part1");
        parts.add("part2");
        archive.setBreakArchiveIds(parts);

        archiveRepository.save(archive);

        return "redirect:/archive/archives";
    }

    private String generateChecksum(Archive archive) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = archive.getName() + archive.getArchiveTypeId() + System.currentTimeMillis();
            byte[] hash = digest.digest(data.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (Exception e) {
            return "error";
        }
    }
}