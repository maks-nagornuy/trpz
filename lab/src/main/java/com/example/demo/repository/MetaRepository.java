package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.demo.model.Meta;


public interface MetaRepository extends MongoRepository<Meta, String> {}