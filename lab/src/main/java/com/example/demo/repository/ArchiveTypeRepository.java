package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.ArchiveType;


public interface ArchiveTypeRepository extends MongoRepository<ArchiveType, String> {}