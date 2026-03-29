package com.scholarsmanuscript.repository;

import com.scholarsmanuscript.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatus(Article.Status status, Pageable pageable);

    Page<Article> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.articleCategories ac WHERE ac.categoryId = :categoryId")
    Page<Article> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword%")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    List<Article> findLatestPublished(Pageable pageable);

    @Query("SELECT YEAR(a.publishedAt) as year FROM Article a WHERE a.status = 'PUBLISHED' AND a.publishedAt IS NOT NULL GROUP BY YEAR(a.publishedAt)")
    List<Integer> findPublishedYears();
}
