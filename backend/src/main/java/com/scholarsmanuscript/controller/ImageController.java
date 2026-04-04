package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.BatchDeleteRequest;
import com.scholarsmanuscript.dto.request.ImageGenerationRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.ImageGenerationHistoryResponse;
import com.scholarsmanuscript.dto.response.ImageGenerationResponse;
import com.scholarsmanuscript.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        ImageGenerationResponse response = imageService.generateImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ImageGenerationHistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ImageGenerationHistoryResponse> histories = imageService.getHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(@PathVariable Long id) {
        imageService.deleteHistory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> deleteAllHistory() {
        imageService.deleteAllHistory();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/history/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDeleteHistory(
            @Valid @RequestBody BatchDeleteRequest request) {
        imageService.deleteHistories(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
