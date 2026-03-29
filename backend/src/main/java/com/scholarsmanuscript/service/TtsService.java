package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.TtsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    private static final String MINI_MAX_API_URL = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(MINI_MAX_API_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public Flux<byte[]> streamSpeech(TtsRequest request) {
        Map<String, Object> body = Map.of(
                "model", "speech-2.8-hd",
                "text", request.getText(),
                "stream", true,
                "voice_setting", Map.of(
                        "voice_id", request.getVoiceId(),
                        "speed", request.getSpeed(),
                        "vol", request.getVol(),
                        "pitch", request.getPitch(),
                        "emotion", request.getEmotion()
                ),
                "audio_setting", Map.of(
                        "sample_rate", request.getSampleRate(),
                        "bitrate", request.getBitrate(),
                        "format", request.getFormat(),
                        "channel", request.getChannel()
                )
        );

        log.info("Calling MiniMax TTS API for text length: {}", request.getText().length());

        return webClient.post()
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(byte[].class);
    }

    public List<Map<String, Object>> getVoices() {
        Map<String, Object> body = Map.of("voice_type", "all");

        log.info("Fetching voice list from MiniMax API");

        return webClient.post()
                .uri("/t2a_v2/voices")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();
    }
}
