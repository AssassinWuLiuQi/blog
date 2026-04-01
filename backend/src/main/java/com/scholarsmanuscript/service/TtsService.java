package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.dto.response.VoiceResponse;
import com.scholarsmanuscript.utils.OkHttpUtil;
import com.scholarsmanuscript.utils.SseEmitterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    private static final String MINI_MAX_API_URL = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    public SseEmitter streamSpeech(TtsRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

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

        Request httpRequest = new Request.Builder()
                .url(MINI_MAX_API_URL + "/t2a_v2")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        OkHttpUtil.executeStream(httpRequest, new OkHttpUtil.OkHttpStreamCallback() {
            @Override
            public void onConnect(Call call) {
                log.info("TTS stream connected");
                SseEmitterUtil.init(emitter, "TTS stream connected");
            }

            @Override
            public void onResponse(Call call, String data) {
                log.info("data: {}", data);
                SseEmitterUtil.data(emitter, data);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                log.error("error", e);
                SseEmitterUtil.completeWithError(emitter, e);
            }

            @Override
            public void onClose(Call call) {
                SseEmitterUtil.complete(emitter);
            }
        });

        return emitter;
    }

    public List<VoiceResponse> getVoices() {
        Map<String, Object> body = Map.of("voice_type", "all");

        log.info("Fetching voice list from MiniMax API");

        Request request = new Request.Builder()
                .url(MINI_MAX_API_URL + "/get_voice")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(request, Map.class);

            if (responseMap == null) {
                return List.of();
            }

            List<VoiceResponse> voices = new ArrayList<>();

            // Parse system_voice
            List<Map<String, Object>> systemVoices = (List<Map<String, Object>>) responseMap.get("system_voice");
            if (systemVoices != null) {
                for (Map<String, Object> voice : systemVoices) {
                    voices.add(parseVoice(voice, "system_voice"));
                }
            }

            // Parse voice_cloning
            List<Map<String, Object>> cloningVoices = (List<Map<String, Object>>) responseMap.get("voice_cloning");
            if (cloningVoices != null) {
                for (Map<String, Object> voice : cloningVoices) {
                    voices.add(parseVoice(voice, "voice_cloning"));
                }
            }

            // Parse voice_generation
            List<Map<String, Object>> generationVoices = (List<Map<String, Object>>) responseMap.get("voice_generation");
            if (generationVoices != null) {
                for (Map<String, Object> voice : generationVoices) {
                    voices.add(parseVoice(voice, "voice_generation"));
                }
            }

            return voices;
        } catch (IOException e) {
            log.error("Error fetching voices", e);
            return List.of();
        }
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
