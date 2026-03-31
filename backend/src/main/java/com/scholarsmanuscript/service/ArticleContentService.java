package com.scholarsmanuscript.service;

import com.scholarsmanuscript.entity.mongo.ArticleContent;
import com.scholarsmanuscript.repository.mongo.ArticleContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleContentService {

    private final ArticleContentRepository articleContentRepository;

    public void saveContent(Long articleId, String content) {
        ArticleContent articleContent = articleContentRepository.findByArticleId(articleId)
                .orElse(ArticleContent.builder().articleId(articleId).build());
        articleContent.setContent(content);
        articleContentRepository.save(articleContent);
    }

    public String getContent(Long articleId) {
        return articleContentRepository.findByArticleId(articleId)
                .map(ArticleContent::getContent)
                .orElse(null);
    }

    public void deleteContent(Long articleId) {
        articleContentRepository.deleteByArticleId(articleId);
    }
}
