package com.example.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.demo.model.Ant;

import java.util.List;

public interface AntRepository extends MongoRepository<Ant, String> {
    List<Ant> findByAnthillId(String anthillId);
}
