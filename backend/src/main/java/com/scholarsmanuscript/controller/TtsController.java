package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.VoiceResponse;
import com.scholarsmanuscript.service.TtsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @GetMapping("/voices")
    public ResponseEntity<ApiResponse<List<VoiceResponse>>> getVoices() {
        List<VoiceResponse> voices = ttsService.getVoices();
        return ResponseEntity.ok(ApiResponse.success(voices));
    }

    @PostMapping(value = "/speech", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<byte[]> textToSpeech(@Valid @RequestBody TtsRequest request) {
        Flux<byte[]> audioStream = ttsService.streamSpeech(request);

        return audioStream;
    }
}
