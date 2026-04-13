package com.scholarsmanuscript.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {

    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
