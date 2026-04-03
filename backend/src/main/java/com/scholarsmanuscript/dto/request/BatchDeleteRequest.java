package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;
}