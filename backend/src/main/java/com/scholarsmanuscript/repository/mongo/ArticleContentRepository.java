package com.scholarsmanuscript.repository.mongo;

import com.scholarsmanuscript.entity.mongo.ArticleContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleContentRepository extends MongoRepository<ArticleContent, String> {

    Optional<ArticleContent> findByArticleId(Long articleId);

    void deleteByArticleId(Long articleId);
}
