package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.service.TtsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @PostMapping(value = "/speech", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Flux<byte[]>> textToSpeech(@Valid @RequestBody TtsRequest request) {
        Flux<byte[]> audioStream = ttsService.streamSpeech(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header("X-Content-Type-Options", "nosniff")
                .body(audioStream);
    }
}
