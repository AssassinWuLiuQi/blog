package com.scholarsmanuscript.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private Long id;
    private String title;
    private String excerpt;
    private String status;
    private String category;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String readTime;
    private List<String> categories;
}
