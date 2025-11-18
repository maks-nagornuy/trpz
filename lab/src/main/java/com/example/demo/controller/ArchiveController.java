package com.example.demo.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.Archive;
import com.example.demo.facade.ArchiveFacade;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveFacade archiveFacade;

    public ArchiveController(ArchiveFacade archiveFacade) {
        this.archiveFacade = archiveFacade;
    }

    @GetMapping("/archives")
    public String showArchives(Model model) {
        model.addAttribute("archives", archiveFacade.getAllArchives());
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

        archiveFacade.createArchive(archive, files);
        return "redirect:/archive/archives";
    }
}
