package com.example.demo.facade;

import com.example.demo.editor.ArchiveEditor;
import com.example.demo.editor.ArchiveEditorFactory;
import com.example.demo.editor.FileNode;
import com.example.demo.model.*;
import com.example.demo.p2p.P2PService;
import com.example.demo.repository.*;
import com.example.demo.service.ArchiveService;
import com.example.demo.visitor.ChecksumVisitor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ArchiveFacade {

    private final ArchiveService archiveService;
    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;
    private final P2PService p2pService;
    private final BreakArchiveRepository breakArchiveRepository;

    private static final String ARCHIVE_DIR = "C:/archives/";
    private static final String RAR_EXE_PATH = "C:\\Program Files\\WinRAR\\Rar.exe";

    public ArchiveFacade(ArchiveService archiveService,
                         ArchiveRepository archiveRepository,
                         UserRepository userRepository,
                         P2PService p2pService,
                         BreakArchiveRepository breakArchiveRepository) {
        this.archiveService = archiveService;
        this.archiveRepository = archiveRepository;
        this.userRepository = userRepository;
        this.p2pService = p2pService;
        this.breakArchiveRepository = breakArchiveRepository;
    }
    public File getArchiveFile(Archive archive) {
        new File(ARCHIVE_DIR).mkdirs();
        return new File(ARCHIVE_DIR + archive.getName() + "." + archive.getArchiveTypeId());
    }

    public Archive getArchiveById(String id) {
        return archiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Archive not found"));
    }

    public List<Archive> getAllArchives() {
        return archiveRepository.findAll();
    }

    public List<Archive> getArchivesForUser(String userId) {
        return archiveRepository.findByUserId(userId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<BreakArchive> getBrokenArchivesForUser(String userId) {
        List<Archive> userArchives = archiveRepository.findByUserId(userId);
        
        if (userArchives == null || userArchives.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> archiveIds = userArchives.stream()
                .map(Archive::getId)
                .collect(Collectors.toList());

        return breakArchiveRepository.findByArchiveIdIn(archiveIds);
    }

    public void deleteGlobalArchive(String id) {
        Archive archive = getArchiveById(id);

        File file = getArchiveFile(archive);
        if (file.exists()) {
            file.delete();
        }

        File splitDir = new File(ARCHIVE_DIR + "split_" + id);
        if (splitDir.exists()) {
            FileSystemUtils.deleteRecursively(splitDir);
        }

        breakArchiveRepository.deleteByArchiveId(id);

        archiveRepository.deleteById(id);
    }

    public void createArchive(Archive archive, MultipartFile[] files) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByEmail(auth.getName());
            if (user != null) archive.setUserId(user.getId());
        }

        List<File> tempFiles = new ArrayList<>();
        Set<Meta> metaSet = new HashSet<>();

        for (MultipartFile multipart : files) {
            if (!multipart.isEmpty()) {
                File temp = File.createTempFile("upload_", "_" + multipart.getOriginalFilename());
                multipart.transferTo(temp);
                tempFiles.add(temp);

                Meta meta = new Meta();
                meta.setFileName(normalizePath(multipart.getOriginalFilename()));
                meta.setSize(multipart.getSize());
                metaSet.add(meta);
            }
        }
        archive.setMeta(metaSet);

        ChecksumVisitor visitor = new ChecksumVisitor();
        archive.accept(visitor);
        archive.setChecksum(visitor.getChecksum());

        File outFile = getArchiveFile(archive);
        try {
            archiveService.createArchive(tempFiles, outFile.getAbsolutePath(), archive.getArchiveTypeId());
        } finally {
            for (File f : tempFiles) f.delete();
        }

        archive.setBreakArchiveIds(Collections.emptySet());
        archiveRepository.save(archive);

        if (p2pService != null) {
            p2pService.sendMessage("User created archive: " + archive.getName());
        }
    }

    public List<BreakArchive> splitArchive(String id, int sizeInMb) throws IOException {
        Archive archive = getArchiveById(id);
        File sourceFile = getArchiveFile(archive);

        if (!sourceFile.exists()) {
            throw new IOException("Файл архіву не знайдено");
        }

        breakArchiveRepository.deleteByArchiveId(id);

        File splitDir = new File(ARCHIVE_DIR + "split_" + id);
        if (splitDir.exists()) {
            FileSystemUtils.deleteRecursively(splitDir);
        }
        splitDir.mkdirs();

        long bytesPerPart = (long) sizeInMb * 1024 * 1024;
        byte[] buffer = new byte[1024 * 8]; 
        List<BreakArchive> createdParts = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(sourceFile);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int partNum = 1;
            int bytesRead;

            while (true) {
                String partName = String.format("%s.%03d", sourceFile.getName(), partNum);
                File partFile = new File(splitDir, partName);

                try (FileOutputStream fos = new FileOutputStream(partFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                    long currentPartSize = 0;
                    while (currentPartSize < bytesPerPart && (bytesRead = bis.read(buffer)) > 0) {
                        bos.write(buffer, 0, bytesRead);
                        currentPartSize += bytesRead;
                    }

                    if (currentPartSize == 0) {
                        partFile.delete();
                        break;
                    }

                    BreakArchive part = new BreakArchive();
                    part.setName(partName);
                    part.setSize(partFile.length());
                    part.setArchiveId(id);
                    breakArchiveRepository.save(part);
                    
                    createdParts.add(part);
                }

                if (sourceFile.length() <= createdParts.stream().mapToLong(BreakArchive::getSize).sum()) {
                    break;
                }
                partNum++;
            }
        }
        return createdParts;
    }

    public File getSplitPartFile(String archiveId, String partName) {
        File splitDir = new File(ARCHIVE_DIR + "split_" + archiveId);
        return new File(splitDir, partName);
    }

    public List<FileNode> listNodes(String archiveId, String currentPath) throws IOException {
        Archive archive = getArchiveById(archiveId);
        File file = getArchiveFile(archive);
        String cp = normalizePath(currentPath);

        Map<String, Boolean> entriesMap = listAllEntries(file, archive.getArchiveTypeId());
        Set<String> allPaths = entriesMap.keySet();

        Set<String> childSet = new HashSet<>();
        for (String e : allPaths) {
            if (!cp.isEmpty()) {
                if (!e.startsWith(cp)) continue;
                String rest = e.substring(cp.length());
                if (rest.startsWith("/")) rest = rest.substring(1);
                if (rest.isEmpty()) continue;
                String child = rest.split("/")[0];
                childSet.add(child);
            } else {
                String child = e.split("/")[0];
                childSet.add(child);
            }
        }

        Map<String, Long> sizeByPath = archive.getMeta() == null ? Map.of() :
                archive.getMeta().stream().collect(Collectors.toMap(Meta::getFileName, Meta::getSize, (a, b) -> a));

        List<FileNode> nodes = new ArrayList<>();
        for (String child : childSet) {
            String full = cp.isEmpty() ? child : cp + "/" + child;
            boolean explicitDir = entriesMap.getOrDefault(full, false);
            boolean hasChildren = allPaths.stream().anyMatch(p -> p.startsWith(full + "/"));
            boolean isDir = explicitDir || hasChildren;
            long size = isDir ? 0L : sizeByPath.getOrDefault(full, 0L);
            nodes.add(new FileNode(child, full, isDir, size));
        }

        nodes.sort((a, b) -> {
            if (a.isDir() != b.isDir()) return a.isDir() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });

        return nodes;
    }

    private Map<String, Boolean> listAllEntries(File archiveFile, String type) throws IOException {
        Map<String, Boolean> map = new HashMap<>();
        if (!archiveFile.exists()) return map;

        if (type.equalsIgnoreCase("zip")) {
            try (ZipFile zip = new ZipFile(archiveFile)) {
                Enumeration<ZipArchiveEntry> entries = zip.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry e = entries.nextElement();
                    map.put(normalizePath(e.getName()), e.isDirectory());
                }
            }
        } else if (type.equalsIgnoreCase("tar.gz")) {
            try (TarArchiveInputStream tis = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(archiveFile)))) {
                TarArchiveEntry entry;
                while ((entry = tis.getNextTarEntry()) != null) {
                    map.put(normalizePath(entry.getName()), entry.isDirectory());
                }
            }
        } else if (type.equalsIgnoreCase("rar")) {
            List<String> rawPaths = listRarEntries(archiveFile);
            for (String raw : rawPaths) {
                boolean looksLikeDir = raw.endsWith("\\") || raw.endsWith("/");
                map.put(normalizePath(raw), looksLikeDir);
            }
        }
        map.remove("");
        return map;
    }

    private List<String> listRarEntries(File rarFile) throws IOException {
        if (!new File(RAR_EXE_PATH).exists()) {
            throw new IOException("Не знайдено Rar.exe за шляхом: " + RAR_EXE_PATH);
        }
        ProcessBuilder pb = new ProcessBuilder(RAR_EXE_PATH, "lb", "-p-", "-c-", rarFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        List<String> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "CP866"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isBlank() && !line.startsWith("RAR ") && !line.contains("Copyright")) {
                    res.add(line);
                }
            }
        }
        try {
            if (!p.waitFor(5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return res;
    }

    private String normalizePath(String p) {
        if (p == null) return "";
        p = p.replace("\\", "/");
        if (p.startsWith("/")) p = p.substring(1);
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        return p;
    }

    public void addFileToArchive(String archiveId, MultipartFile multipart, String currentPath) throws IOException {
        if (multipart == null || multipart.isEmpty()) return;
        Archive archive = getArchiveById(archiveId);
        File archiveFile = getArchiveFile(archive);
        File temp = File.createTempFile("add_", "_" + multipart.getOriginalFilename());
        multipart.transferTo(temp);
        String cp = normalizePath(currentPath);
        String entryName = cp.isEmpty() ? multipart.getOriginalFilename() : cp + "/" + multipart.getOriginalFilename();
        entryName = normalizePath(entryName);
        ArchiveEditor editor = ArchiveEditorFactory.create(archive.getArchiveTypeId());
        editor.addFile(archiveFile, temp, entryName);
        temp.delete();
        updateMetaAddOrReplace(archive, entryName, multipart.getSize());
        regenChecksumAndSave(archive);
    }

    public void deleteFileFromArchive(String archiveId, String filePath) throws IOException {
        Archive archive = getArchiveById(archiveId);
        File archiveFile = getArchiveFile(archive);
        String fp = normalizePath(filePath);
        ArchiveEditor editor = ArchiveEditorFactory.create(archive.getArchiveTypeId());
        editor.deleteFile(archiveFile, fp);
        if (archive.getMeta() != null) archive.getMeta().removeIf(m -> normalizePath(m.getFileName()).equals(fp));
        regenChecksumAndSave(archive);
    }

    public void createFolderInArchive(String archiveId, String folderName, String currentPath) throws IOException {
        Archive archive = getArchiveById(archiveId);
        File archiveFile = getArchiveFile(archive);
        String cp = normalizePath(currentPath);
        String folderPath = cp.isEmpty() ? folderName : cp + "/" + folderName;
        folderPath = normalizePath(folderPath);
        ArchiveEditor editor = ArchiveEditorFactory.create(archive.getArchiveTypeId());
        editor.createFolder(archiveFile, folderPath);
        regenChecksumAndSave(archive);
    }

    public void deleteFolderFromArchive(String archiveId, String folderPath) throws IOException {
        Archive archive = getArchiveById(archiveId);
        File archiveFile = getArchiveFile(archive);
        String fp = normalizePath(folderPath);
        ArchiveEditor editor = ArchiveEditorFactory.create(archive.getArchiveTypeId());
        editor.deleteFolder(archiveFile, fp);
        if (archive.getMeta() != null) {
            String prefix = fp + "/";
            archive.getMeta().removeIf(m -> normalizePath(m.getFileName()).startsWith(prefix));
        }
        regenChecksumAndSave(archive);
    }


    private void addMetaForFolder(Archive archive, File base, String parentInArchive, String currentName) {
        File current = new File(base, currentName);
        String currentArchivePath = parentInArchive.isEmpty() ? currentName : parentInArchive + "/" + currentName;
        currentArchivePath = normalizePath(currentArchivePath);
        if (current.isDirectory()) {
            File[] children = current.listFiles();
            if (children != null) {
                for (File ch : children) addMetaForFolder(archive, current, currentArchivePath, ch.getName());
            }
        } else {
            updateMetaAddOrReplace(archive, currentArchivePath, current.length());
        }
    }

    private void updateMetaAddOrReplace(Archive archive, String filePath, long size) {
        Set<Meta> metaSet = archive.getMeta();
        if (metaSet == null) {
            metaSet = new HashSet<>();
            archive.setMeta(metaSet);
        }
        String fp = normalizePath(filePath);
        metaSet.removeIf(m -> normalizePath(m.getFileName()).equals(fp));
        Meta meta = new Meta();
        meta.setFileName(fp);
        meta.setSize(size);
        metaSet.add(meta);
    }

    private void regenChecksumAndSave(Archive archive) {
        ChecksumVisitor visitor = new ChecksumVisitor();
        archive.accept(visitor);
        archive.setChecksum(visitor.getChecksum());
        archiveRepository.save(archive);
    }

    public boolean verifyIntegrity(String archiveId) throws IOException {
        Archive archive = getArchiveById(archiveId);
        File file = getArchiveFile(archive);
        if (!file.exists()) return false;
        ChecksumVisitor visitor = new ChecksumVisitor();
        archive.accept(visitor);
        return visitor.getChecksum().equals(archive.getChecksum());
    }
}