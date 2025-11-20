package com.example.demo.facade;
import com.example.demo.model.Archive;
import com.example.demo.model.Meta;
import com.example.demo.model.User;
import com.example.demo.p2p.P2PService;
import com.example.demo.repository.ArchiveRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ArchiveService;
import com.example.demo.visitor.ChecksumVisitor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.*;
@Component
public class ArchiveFacade {
    private final ArchiveService archiveService;
    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;
    private final P2PService p2pService;

    public ArchiveFacade(
            ArchiveService archiveService,
            ArchiveRepository archiveRepository,
            UserRepository userRepository,
            P2PService p2pService
    ) {
        this.archiveService = archiveService;
        this.archiveRepository = archiveRepository;
        this.userRepository = userRepository;
        this.p2pService = p2pService;
    }
    public void createArchive(Archive archive, MultipartFile[] files) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // email — логін
        User user = userRepository.findByEmail(email);
        archive.setUserId(user.getId());
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
        ChecksumVisitor v = new ChecksumVisitor();
        archive.accept(v);
        archive.setChecksum(v.getChecksum());
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
        String message = "User " + archive.getUserId() + " created archive: " + archive.getName() + "." + archive.getArchiveTypeId();
        p2pService.sendMessage(message);
    }
    public List<Archive> getAllArchives() {
        return archiveRepository.findAll();
    }
}
