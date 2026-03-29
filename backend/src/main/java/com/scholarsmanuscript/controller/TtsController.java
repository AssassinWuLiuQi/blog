package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.service.TtsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @GetMapping("/voices")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVoices() {
        List<Map<String, Object>> voices = ttsService.getVoices();
        return ResponseEntity.ok(ApiResponse.success(voices));
    }

    @PostMapping(value = "/speech", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Flux<byte[]>> textToSpeech(@Valid @RequestBody TtsRequest request) {
        Flux<byte[]> audioStream = ttsService.streamSpeech(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("X-Content-Type-Options", "nosniff")
                .body(audioStream);
    }
}
