package com.scholarsmanuscript.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "article_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleContent {

    @Id
    private String id;

    private Long articleId;

    private String content;
}
