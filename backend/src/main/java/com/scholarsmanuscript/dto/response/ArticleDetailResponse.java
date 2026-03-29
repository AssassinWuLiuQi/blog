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
public class ArticleDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String status;
    private String author;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private List<String> categories;
}
