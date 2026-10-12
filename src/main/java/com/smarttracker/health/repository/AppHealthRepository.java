package com.smarttracker.health.repository;

import com.smarttracker.health.entity.AppHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppHealthRepository extends JpaRepository<AppHealth, Long> {
}
