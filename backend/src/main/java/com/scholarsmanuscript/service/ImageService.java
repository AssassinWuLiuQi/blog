package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scholarsmanuscript.dto.request.ImageGenerationRequest;
import com.scholarsmanuscript.dto.response.ImageGenerationResponse;
import com.scholarsmanuscript.utils.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String MINI_MAX_API_URL = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

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

        Request httpRequest = new Request.Builder()
                .url(MINI_MAX_API_URL + "/image_generation")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(httpRequest, Map.class);
            return parseResponse(responseMap);
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
}
