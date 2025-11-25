package com.example.demo.controller;

import com.example.demo.editor.FileNode;
import com.example.demo.facade.ArchiveFacade;
import com.example.demo.model.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.List;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    private final ArchiveFacade archiveFacade;

    public ArchiveController(ArchiveFacade archiveFacade) {
        this.archiveFacade = archiveFacade;
    }

    @GetMapping("/archives")
    public String showArchives(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = archiveFacade.getUserByEmail(email);

        List<Archive> archives = archiveFacade.getArchivesForUser(user.getId());
        model.addAttribute("archives", archives);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        model.addAttribute("isAdmin", isAdmin);

        return "archives";
    }

    @GetMapping("/create")
    public String createArchiveForm(Model model) {
        model.addAttribute("archive", new Archive());
        model.addAttribute("archiveTypes", new String[]{"zip", "tar.gz", "rar"});
        return "create_archive";
    }

    @PostMapping("/create")
    public String createArchiveSubmit(@ModelAttribute("archive") Archive archive, @RequestParam("files") MultipartFile[] files) throws IOException {
        archiveFacade.createArchive(archive, files);
        return "redirect:/archive/archives";
    }

    @GetMapping("/view/{id}")
    public String viewArchive(@PathVariable String id, @RequestParam(value = "path", required = false, defaultValue = "") String path, @RequestParam(value = "verify", required = false) String verify, Model model) throws IOException {
        Archive archive = archiveFacade.getArchiveById(id);
        List<FileNode> nodes = archiveFacade.listNodes(id, path);
        model.addAttribute("archive", archive);
        model.addAttribute("nodes", nodes);
        model.addAttribute("currentPath", path);
        model.addAttribute("verify", verify);
        return "archive_view";
    }

    @PostMapping("/edit/{id}/add-file")
    public String addFile(@PathVariable String id, @RequestParam("file") MultipartFile file, @RequestParam(value = "path", defaultValue = "") String path) throws IOException {
        archiveFacade.addFileToArchive(id, file, path);
        return "redirect:/archive/view/" + id + "?path=" + path;
    }

    @PostMapping("/edit/{id}/delete-file")
    public String deleteFile(@PathVariable String id, @RequestParam("filePath") String filePath, @RequestParam(value = "path", defaultValue = "") String path) throws IOException {
        archiveFacade.deleteFileFromArchive(id, filePath);
        return "redirect:/archive/view/" + id + "?path=" + path;
    }

    @PostMapping("/edit/{id}/create-folder")
    public String createFolder(@PathVariable String id, @RequestParam("folderName") String folderName, @RequestParam(value = "path", defaultValue = "") String path) throws IOException {
        archiveFacade.createFolderInArchive(id, folderName, path);
        return "redirect:/archive/view/" + id + "?path=" + path;
    }

    @PostMapping("/edit/{id}/delete-folder")
    public String deleteFolder(@PathVariable String id, @RequestParam("folderPath") String folderPath, @RequestParam(value = "path", defaultValue = "") String path) throws IOException {
        archiveFacade.deleteFolderFromArchive(id, folderPath);
        return "redirect:/archive/view/" + id + "?path=" + path;
    }

    @PostMapping("/edit/{id}/add-folder")
    public String addFolder(@PathVariable String id, @RequestParam("folderFiles") MultipartFile[] folderFiles, @RequestParam(value = "folderRoot") String folderRoot, @RequestParam(value = "path", required = false, defaultValue = "") String path) throws IOException {
        File tmpRoot = new File(System.getProperty("java.io.tmpdir"), "upl_" + System.nanoTime());
        tmpRoot.mkdirs();
        try {
            for (MultipartFile mf : folderFiles) {
                if (mf.isEmpty()) continue;
                String rel = mf.getOriginalFilename().replace("\\", "/");
                File target = new File(tmpRoot, rel);
                target.getParentFile().mkdirs();
                mf.transferTo(target);
            }
            File realFolder = new File(tmpRoot, folderRoot);
            if (!realFolder.exists()) {
                File[] subs = tmpRoot.listFiles(File::isDirectory);
                if (subs != null && subs.length > 0) realFolder = subs[0];
            }
        } finally {
            FileSystemUtils.deleteRecursively(tmpRoot);
        }
        return "redirect:/archive/view/" + id + "?path=" + path;
    }

    @PostMapping("/check/{id}")
    public String verifyArchive(@PathVariable String id, @RequestParam(value = "path", defaultValue = "") String path) throws IOException {
        boolean ok = archiveFacade.verifyIntegrity(id);
        return "redirect:/archive/view/" + id + "?path=" + path + "&verify=" + ok;
    }

    @GetMapping("/download/{id}")
    public void downloadArchive(@PathVariable String id, HttpServletResponse response) throws IOException {
        Archive archive = archiveFacade.getArchiveById(id);
        File file = archiveFacade.getArchiveFile(archive);
        if (!file.exists()) {
            response.sendError(404, "Archive not found");
            return;
        }
        String originalName = file.getName();
        String asciiName = originalName.replaceAll("[^A-Za-z0-9\\.\\-_]", "_");
        String utf8Name = java.net.URLEncoder.encode(originalName, "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + utf8Name);
        try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @PostMapping("/split/{id}")
    public String splitArchive(@PathVariable String id, @RequestParam("sizeMb") int sizeMb, Model model) throws IOException {
        List<BreakArchive> parts = archiveFacade.splitArchive(id, sizeMb);
        Archive archive = archiveFacade.getArchiveById(id);
        model.addAttribute("archive", archive);
        model.addAttribute("parts", parts);
        return "split_result";
    }

    @PostMapping("/delete/{id}")
    public String deleteGlobalArchive(@PathVariable String id) {
        archiveFacade.deleteGlobalArchive(id);
        return "redirect:/archive/archives";
    }

    @GetMapping("/download-part/{id}")
    public void downloadPart(@PathVariable String id, @RequestParam("name") String partName, HttpServletResponse response) throws IOException {
        File part = archiveFacade.getSplitPartFile(id, partName);
        if (!part.exists()) {
            response.sendError(404, "Part file not found on disk");
            return;
        }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + part.getName() + "\"");
        response.setContentLengthLong(part.length());
        try (InputStream in = new FileInputStream(part); OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        }
    }
}