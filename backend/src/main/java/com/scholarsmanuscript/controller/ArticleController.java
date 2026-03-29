package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ArticleRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ArticleDetailResponse;
import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long category) {
        Page<ArticleResponse> articles = articleService.getArticles(page, size, status, category);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleById(@PathVariable Long id) {
        ArticleDetailResponse article = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse article = articleService.createArticle(request);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {
        ArticleResponse article = articleService.updateArticle(id, request);
        return ResponseEntity.ok(ApiResponse.success(article));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.searchArticles(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }
}
