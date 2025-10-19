package org.example.iqtestweb.repository;


import org.example.iqtestweb.entity.TestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    List<TestSession> findByUserUserId(Long userId);
    List<TestSession> findAllByOrderByCompletedAtDesc();
}
