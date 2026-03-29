package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.ArticleRequest;
import com.scholarsmanuscript.dto.response.ArticleDetailResponse;
import com.scholarsmanuscript.dto.response.ArticleResponse;
import com.scholarsmanuscript.entity.Article;
import com.scholarsmanuscript.entity.Category;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ArticleRepository;
import com.scholarsmanuscript.repository.CategoryRepository;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Page<ArticleResponse> getArticles(int page, int size, String status, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Article> articles;
        if (categoryId != null) {
            articles = articleRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null && !status.isEmpty()) {
            articles = articleRepository.findByStatus(Article.Status.valueOf(status.toUpperCase()), pageable);
        } else {
            articles = articleRepository.findAll(pageable);
        }

        return articles.map(this::mapToArticleResponse);
    }

    public ArticleDetailResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
        return mapToArticleDetailResponse(article);
    }

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .excerpt(request.getExcerpt())
                .status(Article.Status.valueOf(request.getStatus().toUpperCase()))
                .author(author)
                .build();

        if (article.getStatus() == Article.Status.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        Article savedArticle = articleRepository.save(article);

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                savedArticle.addCategory(category);
            }
            savedArticle = articleRepository.save(savedArticle);
        }

        return mapToArticleResponse(savedArticle);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setExcerpt(request.getExcerpt());

        Article.Status newStatus = Article.Status.valueOf(request.getStatus().toUpperCase());
        if (newStatus == Article.Status.PUBLISHED && article.getStatus() != Article.Status.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setStatus(newStatus);

        if (request.getCategoryIds() != null) {
            article.getArticleCategories().clear();
            for (Long categoryId : request.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                article.addCategory(category);
            }
        }

        Article updatedArticle = articleRepository.save(article);
        return mapToArticleResponse(updatedArticle);
    }

    @Transactional
    public void deleteArticle(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        if (!article.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        articleRepository.delete(article);
    }

    public Page<ArticleResponse> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return articleRepository.searchByKeyword(keyword, pageable).map(this::mapToArticleResponse);
    }

    private ArticleResponse mapToArticleResponse(Article article) {
        List<String> categories = article.getArticleCategories().stream()
                .map(ac -> {
                    Category cat = categoryRepository.findById(ac.getCategoryId()).orElse(null);
                    return cat != null ? cat.getName() : null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());

        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .excerpt(article.getExcerpt())
                .status(article.getStatus().name())
                .author(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .publishedAt(article.getPublishedAt())
                .categories(categories)
                .readTime(calculateReadTime(article.getContent()))
                .build();
    }

    private ArticleDetailResponse mapToArticleDetailResponse(Article article) {
        List<String> categories = article.getArticleCategories().stream()
                .map(ac -> {
                    Category cat = categoryRepository.findById(ac.getCategoryId()).orElse(null);
                    return cat != null ? cat.getName() : null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toList());

        return ArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .excerpt(article.getExcerpt())
                .status(article.getStatus().name())
                .author(article.getAuthor().getUsername())
                .authorId(article.getAuthor().getId())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .categories(categories)
                .build();
    }

    private String calculateReadTime(String content) {
        if (content == null || content.isEmpty()) {
            return "1分钟";
        }
        int wordCount = content.length() / 500;
        return Math.max(1, wordCount) + "分钟";
    }
}
