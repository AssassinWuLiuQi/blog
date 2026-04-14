package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scholarsmanuscript.dto.request.LyricsGenerationRequest;
import com.scholarsmanuscript.dto.request.MusicGenerationRequest;
import com.scholarsmanuscript.dto.response.LyricsGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicGenerationResponse;
import com.scholarsmanuscript.dto.response.MusicHistoryResponse;
import com.scholarsmanuscript.entity.MusicGenerationHistory;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.MusicGenerationHistoryRepository;
import com.scholarsmanuscript.repository.UserRepository;
import com.scholarsmanuscript.utils.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private static final String MINIMAX_API_BASE = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    private final MinioService minioService;
    private final MusicGenerationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    // ==================== Lyrics Generation ====================

    public LyricsGenerationResponse generateLyrics(LyricsGenerationRequest request) {
        log.info("Calling MiniMax Lyrics Generation API, prompt: {}", request.getPrompt());

        Map<String, Object> body = new HashMap<>();
        body.put("mode", request.getMode());
        body.put("prompt", request.getPrompt());

        Request httpRequest = new Request.Builder()
                .url(MINIMAX_API_BASE + "/lyrics_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(
                        JSON.toJSONString(body),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            return parseLyricsResponse(responseMap);
        } catch (IOException e) {
            log.error("Error calling MiniMax Lyrics Generation API", e);
            return LyricsGenerationResponse.builder()
                    .statusCode(500)
                    .statusMsg("API call failed: " + e.getMessage())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private LyricsGenerationResponse parseLyricsResponse(Map<String, Object> responseMap) {
        LyricsGenerationResponse.LyricsGenerationResponseBuilder builder = LyricsGenerationResponse.builder();

        Map<String, Object> baseResp = (Map<String, Object>) responseMap.get("base_resp");
        if (baseResp != null) {
            builder.statusCode((Integer) baseResp.get("status_code"));
            builder.statusMsg((String) baseResp.get("status_msg"));
        }

        // MiniMax lyrics API returns lyrics in data.text
        Object data = responseMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            String lyrics = (String) dataMap.get("text");
            if (lyrics == null) {
                lyrics = (String) dataMap.get("lyrics");
            }
            builder.lyrics(lyrics);
        }

        return builder.build();
    }

    // ==================== Music Generation ====================

    public MusicGenerationResponse generateMusic(MusicGenerationRequest request) {
        log.info("Calling MiniMax Music Generation API, model: {}, prompt: {}",
                request.getModel(), request.getPrompt());

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());
        body.put("output_format", "url");
        body.put("is_instrumental", request.isInstrumental());

        Map<String, Object> audioSetting = new HashMap<>();
        audioSetting.put("sample_rate", 44100);
        audioSetting.put("bitrate", 256000);
        audioSetting.put("format", "mp3");
        body.put("audio_setting", audioSetting);

        String lyrics = request.getLyrics();
        if (lyrics != null && !lyrics.isBlank()) {
            body.put("lyrics", lyrics);
        } else if (!request.isInstrumental()) {
            // no lyrics provided and not instrumental → let MiniMax auto-generate
            body.put("lyrics_optimizer", true);
        }

        Request httpRequest = new Request.Builder()
                .url(MINIMAX_API_BASE + "/music_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(
                        JSON.toJSONString(body),
                        okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        MusicGenerationResponse response;
        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            response = parseMusicResponse(responseMap);
        } catch (IOException e) {
            log.error("Error calling MiniMax Music Generation API", e);
            response = MusicGenerationResponse.builder()
                    .statusCode(500)
                    .statusMsg("API call failed: " + e.getMessage())
                    .build();
        }

        // Upload audio to MinIO
        if (response.getStatusCode() != null && response.getStatusCode() == 0
                && response.getAudioUrl() != null && !response.getAudioUrl().isBlank()) {
            try {
                String minioUrl = minioService.uploadAudioFromUrl(response.getAudioUrl());
                response.setAudioUrl(minioUrl);
                log.info("Uploaded audio to MinIO: {}", minioUrl);
            } catch (Exception e) {
                log.error("Failed to upload audio to MinIO", e);
                response.setStatusCode(500);
                response.setStatusMsg("Failed to upload audio: " + e.getMessage());
            }
        }

        saveHistory(request, response);
        return response;
    }

    @SuppressWarnings("unchecked")
    private MusicGenerationResponse parseMusicResponse(Map<String, Object> responseMap) {
        MusicGenerationResponse.MusicGenerationResponseBuilder builder = MusicGenerationResponse.builder();

        Map<String, Object> baseResp = (Map<String, Object>) responseMap.get("base_resp");
        if (baseResp != null) {
            builder.statusCode((Integer) baseResp.get("status_code"));
            builder.statusMsg((String) baseResp.get("status_msg"));
        }

        builder.traceId((String) responseMap.get("trace_id"));

        // data.audio contains the URL when output_format is "url"
        Object data = responseMap.get("data");
        if (data instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) data;
            builder.audioUrl((String) dataMap.get("audio"));
        }

        return builder.build();
    }

    // ==================== History ====================

    @Transactional
    public void saveHistory(MusicGenerationRequest request, MusicGenerationResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MusicGenerationHistory history = MusicGenerationHistory.builder()
                .user(user)
                .prompt(request.getPrompt())
                .lyrics(request.getLyrics())
                .audioUrl(response.getAudioUrl())
                .model(request.getModel())
                .isInstrumental(request.isInstrumental())
                .statusCode(response.getStatusCode())
                .statusMsg(response.getStatusMsg())
                .traceId(response.getTraceId())
                .build();

        historyRepository.save(history);
        log.info("Saved music generation history for user: {}", username);
    }

    public Page<MusicHistoryResponse> getHistory(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        return historyRepository.findByUserIdAndNotDeleted(user.getId(), pageable)
                .map(MusicHistoryResponse::fromEntity);
    }

    @Transactional
    public void deleteHistory(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        MusicGenerationHistory history = historyRepository
                .findByIdAndUserIdAndNotDeleted(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "History not found"));

        history.softDelete();
        historyRepository.save(history);
        log.info("Soft deleted music history id: {} for user: {}", id, username);
    }
}
