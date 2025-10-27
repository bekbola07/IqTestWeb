package org.example.iqtestweb.repository;


import org.example.iqtestweb.entity.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Long> {
}