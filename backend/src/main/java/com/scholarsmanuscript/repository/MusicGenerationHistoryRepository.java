package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.MusicGenerationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicGenerationHistoryRepository extends JpaRepository<MusicGenerationHistory, Long> {

    @Query("SELECT h FROM MusicGenerationHistory h WHERE h.user.id = :userId AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<MusicGenerationHistory> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM MusicGenerationHistory h WHERE h.id = :id AND h.user.id = :userId AND h.deletedAt IS NULL")
    Optional<MusicGenerationHistory> findByIdAndUserIdAndNotDeleted(@Param("id") Long id, @Param("userId") Long userId);
}
