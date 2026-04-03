package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.ImageGenerationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageGenerationHistoryRepository extends JpaRepository<ImageGenerationHistory, Long> {

    @Query("SELECT h FROM ImageGenerationHistory h WHERE h.user.id = :userId AND h.deletedAt IS NULL ORDER BY h.createdAt DESC")
    Page<ImageGenerationHistory> findByUserIdAndNotDeleted(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT h FROM ImageGenerationHistory h WHERE h.id = :id AND h.user.id = :userId AND h.deletedAt IS NULL")
    Optional<ImageGenerationHistory> findByIdAndUserIdAndNotDeleted(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT COUNT(h) FROM ImageGenerationHistory h WHERE h.user.id = :userId AND h.deletedAt IS NULL")
    long countByUserIdAndNotDeleted(@Param("userId") Long userId);

    @Query("SELECT h FROM ImageGenerationHistory h WHERE h.id IN :ids AND h.user.id = :userId AND h.deletedAt IS NULL")
    List<ImageGenerationHistory> findByIdInAndUserIdAndNotDeleted(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
