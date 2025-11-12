package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.Archive;


public interface ArchiveRepository extends MongoRepository<Archive, String> {}