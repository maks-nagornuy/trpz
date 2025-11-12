package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.BreakArchive;


public interface BreakArchiveRepository extends MongoRepository<BreakArchive, String> {}
