package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.ImageGenerationRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ImageGenerationResponse;
import com.scholarsmanuscript.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ImageGenerationResponse>> generate(
            @Valid @RequestBody ImageGenerationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(imageService.generateImage(request)));
    }
}
