package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.dto.response.VoiceResponse;
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
                .uri("/t2a_v2")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(byte[].class);
    }

    @SuppressWarnings("unchecked")
    public List<VoiceResponse> getVoices() {
        Map<String, Object> body = Map.of("voice_type", "all");

        log.info("Fetching voice list from MiniMax API");

        Map<String, Object> response = webClient.post()
                .uri("/get_voice")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            return List.of();
        }

        List<VoiceResponse> voices = new java.util.ArrayList<>();

        // Parse system_voice
        List<Map<String, Object>> systemVoices = (List<Map<String, Object>>) response.get("system_voice");
        if (systemVoices != null) {
            for (Map<String, Object> voice : systemVoices) {
                voices.add(parseVoice(voice, "system_voice"));
            }
        }

        // Parse voice_cloning
        List<Map<String, Object>> cloningVoices = (List<Map<String, Object>>) response.get("voice_cloning");
        if (cloningVoices != null) {
            for (Map<String, Object> voice : cloningVoices) {
                voices.add(parseVoice(voice, "voice_cloning"));
            }
        }

        // Parse voice_generation
        List<Map<String, Object>> generationVoices = (List<Map<String, Object>>) response.get("voice_generation");
        if (generationVoices != null) {
            for (Map<String, Object> voice : generationVoices) {
                voices.add(parseVoice(voice, "voice_generation"));
            }
        }

        return voices;
    }

    private VoiceResponse parseVoice(Map<String, Object> voice, String type) {
        return VoiceResponse.builder()
                .voiceId((String) voice.get("voice_id"))
                .voiceName((String) voice.get("voice_name"))
                .description((List<String>) voice.get("description"))
                .createdTime((String) voice.get("created_time"))
                .type(type)
                .build();
    }
}
