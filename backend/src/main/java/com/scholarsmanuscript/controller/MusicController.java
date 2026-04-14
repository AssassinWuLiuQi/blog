package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.LyricsGenerationRequest;
import com.scholarsmanuscript.dto.request.MusicGenerationRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.LyricsGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicHistoryResponse;
import com.scholarsmanuscript.service.MusicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @PostMapping("/lyrics")
    public ResponseEntity<ApiResponse<LyricsGenerationResponse>> generateLyrics(
            @Valid @RequestBody LyricsGenerationRequest request) {
        LyricsGenerationResponse response = musicService.generateLyrics(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MusicGenerationResponse>> generateMusic(
            @Valid @RequestBody MusicGenerationRequest request) {
        MusicGenerationResponse response = musicService.generateMusic(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<MusicHistoryResponse>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MusicHistoryResponse> histories = musicService.getHistory(page, size);
        return ResponseEntity.ok(ApiResponse.success(histories));
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(@PathVariable Long id) {
        musicService.deleteHistory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
