package com.scholarsmanuscript.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;
}
