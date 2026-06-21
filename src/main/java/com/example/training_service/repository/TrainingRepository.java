package com.example.training_service.repository;

import com.example.training_service.model.Training;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingRepository extends JpaRepository<Training, UUID>, JpaSpecificationExecutor<Training> {

    @Query("SELECT t FROM Training t WHERE t.user_id = :userId")
    Page<Training> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Training t WHERE t.user_id = :userId")
    List<Training> findByUserId(@Param("userId") UUID userId);
}
