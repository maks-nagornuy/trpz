package com.example.demo.repository;

import java.util.List;
import java.util.Collection; 

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.demo.model.BreakArchive;

public interface BreakArchiveRepository extends MongoRepository<BreakArchive, String> {  
    List<BreakArchive> findByArchiveId(String archiveId); 
    void deleteByArchiveId(String archiveId);
    List<BreakArchive> findByArchiveIdIn(Collection<String> archiveIds);
}