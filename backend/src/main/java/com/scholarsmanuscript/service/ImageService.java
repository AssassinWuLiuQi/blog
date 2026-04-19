package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scholarsmanuscript.dto.request.ImageGenerationRequest;
import com.scholarsmanuscript.dto.response.ImageGenerationHistoryResponse;
import com.scholarsmanuscript.dto.response.ImageGenerationResponse;
import com.scholarsmanuscript.entity.ImageGenerationHistory;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ImageGenerationHistoryRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.scholarsmanuscript.dto.request.ImageGenerationRequest.SubjectReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String MINI_MAX_API_URL = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    private final MinioService minioService;
    private final ImageGenerationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        log.info("Calling MiniMax Image Generation API for prompt length: {}", request.getPrompt().length());

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("prompt", request.getPrompt());
        body.put("aspect_ratio", request.getAspectRatio());
        body.put("n", request.getN());
        body.put("response_format", request.getResponseFormat());
        body.put("prompt_optimizer", request.getPromptOptimizer());
        body.put("aigc_watermark", request.getAigcWatermark());

        // Handle subject reference (image-to-image)
        if (request.getSubjectReference() != null && !request.getSubjectReference().isEmpty()) {
            List<Map<String, String>> subjectRefs = new ArrayList<>();
            for (SubjectReference ref : request.getSubjectReference()) {
                String imageFile = ref.getImageFile();
                // If image is base64 encoded, upload to MinIO first
                if (imageFile != null && imageFile.startsWith("data:image")) {
                    try {
                        imageFile = minioService.uploadFromBase64(imageFile, "reference-" + System.currentTimeMillis() + ".png");
                        log.info("Uploaded base64 reference image to MinIO: {}", imageFile);
                    } catch (Exception e) {
                        log.error("Failed to upload base64 reference image to MinIO", e);
                        return ImageGenerationResponse.builder()
                                .statusCode(500)
                                .statusMsg("Failed to upload reference image: " + e.getMessage())
                                .build();
                    }
                }
                Map<String, String> refMap = new HashMap<>();
                refMap.put("type", ref.getType() != null ? ref.getType() : "character");
                refMap.put("image_file", imageFile);
                subjectRefs.add(refMap);
            }
            body.put("subject_reference", subjectRefs);
        }

        Request httpRequest = new Request.Builder()
                .url(MINI_MAX_API_URL + "/image_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            ImageGenerationResponse response = parseResponse(responseMap);

            // Upload images to MinIO and replace URLs
            if (response.getStatusCode() != null && response.getStatusCode() == 0
                    && response.getImageUrls() != null && !response.getImageUrls().isEmpty()) {
                try {
                    List<String> minioUrls = response.getImageUrls().stream()
                            .map(minioService::uploadFromUrl)
                            .collect(Collectors.toList());
                    response.setImageUrls(minioUrls);
                } catch (Exception e) {
                    log.error("Failed to upload images to MinIO", e);
                    response.setStatusCode(500);
                    response.setStatusMsg("Failed to upload images to MinIO: " + e.getMessage());
                }
            } else if (response.getStatusCode() != null && response.getStatusCode() != 0) {
                log.error("Image generation failed: statusCode={}, statusMsg={}",
                        response.getStatusCode(), response.getStatusMsg());
            }

            // Save to history
            saveHistory(request, response);

            return response;
        } catch (IOException e) {
            log.error("Error calling MiniMax Image Generation API", e);
            return ImageGenerationResponse.builder()
                    .statusCode(500)
                    .statusMsg("API call failed: " + e.getMessage())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private ImageGenerationResponse parseResponse(Map<String, Object> responseMap) {
        ImageGenerationResponse.ImageGenerationResponseBuilder builder = ImageGenerationResponse.builder();

        // Parse base_resp
        Map<String, Object> baseResp = (Map<String, Object>) responseMap.get("base_resp");
        if (baseResp != null) {
            builder.statusCode((Integer) baseResp.get("status_code"));
            builder.statusMsg((String) baseResp.get("status_msg"));
        }

        // Parse metadata
        Map<String, Object> metadata = (Map<String, Object>) responseMap.get("metadata");
        if (metadata != null) {
            builder.successCount(safeGetInt(metadata, "success_count"));
            builder.failedCount(safeGetInt(metadata, "failed_count"));
        }

        // Parse image URLs
        List<String> imageUrls = new ArrayList<>();
        JSONObject data = (JSONObject) responseMap.get("data");
        if (data != null) {
            Object imageUrlsObj = data.get("image_urls");
            if (imageUrlsObj instanceof List) {
                imageUrls = (List<String>) imageUrlsObj;
            }
            builder.taskId(data.getString("task_id"));
        }
        builder.imageUrls(imageUrls);

        return builder.build();
    }

    private Integer safeGetInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) return Integer.valueOf((String) value);
        return null;
    }

    // ==================== History Methods ====================

    @Transactional
    public void saveHistory(ImageGenerationRequest request, ImageGenerationResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String imageUrlsJson = null;
        if (response.getImageUrls() != null && !response.getImageUrls().isEmpty()) {
            imageUrlsJson = JSON.toJSONString(response.getImageUrls());
        }

        ImageGenerationHistory history = ImageGenerationHistory.builder()
                .user(user)
                .prompt(request.getPrompt())
                .imageUrls(imageUrlsJson)
                .model(request.getModel())
                .aspectRatio(request.getAspectRatio())
                .successCount(response.getSuccessCount())
                .failedCount(response.getFailedCount())
                .taskId(response.getTaskId())
                .statusCode(response.getStatusCode())
                .statusMsg(response.getStatusMsg())
                .build();

        historyRepository.save(history);
        log.info("Saved image generation history for user: {}, prompt length: {}", username, request.getPrompt().length());
    }

    public Page<ImageGenerationHistoryResponse> getHistory(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);
        Page<ImageGenerationHistory> histories = historyRepository.findByUserIdAndNotDeleted(user.getId(), pageable);

        return histories.map(ImageGenerationHistoryResponse::fromEntity);
    }

    @Transactional
    public void deleteHistory(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ImageGenerationHistory history = historyRepository.findByIdAndUserIdAndNotDeleted(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "History not found"));

        history.softDelete();
        historyRepository.save(history);
        log.info("Soft deleted image generation history id: {} for user: {}", id, username);
    }

    @Transactional
    public void deleteAllHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, 100);
        Page<ImageGenerationHistory> histories = historyRepository.findByUserIdAndNotDeleted(user.getId(), pageable);

        for (ImageGenerationHistory history : histories) {
            history.softDelete();
            historyRepository.save(history);
        }
        log.info("Soft deleted all image generation histories for user: {}", username);
    }

    @Transactional
    public void deleteHistories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<ImageGenerationHistory> histories = historyRepository.findByIdInAndUserIdAndNotDeleted(ids, user.getId());
        for (ImageGenerationHistory history : histories) {
            history.softDelete();
            historyRepository.save(history);
        }
        log.info("Batch soft deleted {} image generation histories for user: {}", histories.size(), username);
    }
}
