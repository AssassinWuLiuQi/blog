package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.scholarsmanuscript.dto.request.TtsRequest;
import com.scholarsmanuscript.dto.response.VoiceResponse;
import com.scholarsmanuscript.utils.OkHttpUtil;
import com.scholarsmanuscript.utils.SseEmitterUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MiniMax TTS 语音合成服务
 *
 * @see <a href="https://platform.minimaxi.com/docs/api-reference/speech-t2a-http">MiniMax TTS API</a>
 * @see <a href="https://platform.minimaxi.com/docs/api-reference/speech-t2a-streaming">MiniMax TTS 流式API</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    /**
     * MiniMax API 基础地址
     * T2A v2 端点: /t2a_v2（文本转语音）
     * 获取音色列表: /get_voice
     */
    private static final String MINI_MAX_API_URL = "https://api.minimaxi.com/v1";

    @Value("${miniMax.api-key:}")
    private String apiKey;

    /**
     * 流式语音合成
     *
     * @param request TTS请求参数
     * @return SseEmitter 用于流式传输音频数据
     *
     * @apiNote 请求示例:
     * <pre>
     * POST /api/tts/speech
     * {
     *   "text": "你好，这是测试音频",
     *   "voiceId": "male-qn-qingse",
     *   "speed": 1.0,
     *   "emotion": "happy"
     * }
     * </pre>
     *
     * @apiNote 响应为 SSE 流事件:
     * <pre>
     * event: message
     * data: {"step":100,"data":"hex编码的音频数据"}
     * data: {"step":1000}  // 流结束
     * </pre>
     */
    public SseEmitter streamSpeech(TtsRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 构建完整的请求体
        Map<String, Object> body = buildRequestBody(request);

        log.info("Calling MiniMax TTS API - model: speech-2.8-hd, text length: {}, voice: {}",
                request.getText().length(), request.getVoiceId());

        Request httpRequest = new Request.Builder()
                .url(MINI_MAX_API_URL + "/t2a_v2")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        // 使用 executeStream 处理流式响应
        OkHttpUtil.executeStream(httpRequest, new OkHttpUtil.OkHttpStreamCallback() {
            @Override
            public void onOpen(EventSource eventSource) {
                SseEmitterUtil.init(emitter, "Connected");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                JSONObject dataJson = JSONObject.parseObject(data).getJSONObject("data");
                SseEmitterUtil.data(emitter, dataJson.getString("audio"));
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t) {
                log.error("TTS stream error: {}", t.getMessage());
                if (t instanceof IOException) {
                    SseEmitterUtil.completeWithError(emitter, (IOException) t);
                } else {
                    SseEmitterUtil.error(emitter, t.getMessage());
                    emitter.complete();
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                SseEmitterUtil.complete(emitter);
            }
        });

        return emitter;
    }

    /**
     * 构建 MiniMax TTS API 请求体
     *
     * @param request TTS请求参数
     * @return 完整的请求体Map
     *
     * @apiNote 请求体结构:
     * <pre>
     * {
     *   "model": "speech-2.8-hd",
     *   "text": "...",
     *   "stream": true,
     *   "voice_setting": {
     *     "voice_id": "...",
     *     "speed": 1.0,
     *     "vol": 1,
     *     "pitch": 0,
     *     "emotion": "happy",
     *     "text_normalization": false,
     *     "latex_read": false
     *   },
     *   "audio_setting": {
     *     "sample_rate": 32000,
     *     "bitrate": 128000,
     *     "format": "pcm",
     *     "channel": 1,
     *     "force_cbr": false
     *   },
     *   "voice_modify": {
     *     "pitch": 0,
     *     "intensity": 0,
     *     "timbre": 0,
     *     "sound_effects": null
     *   },
     *   "output_format": "hex",
     *   "subtitle_enable": false,
     *   "aigc_watermark": false
     * }
     * </pre>
     */
    private Map<String, Object> buildRequestBody(TtsRequest request) {
        Map<String, Object> body = new HashMap<>();

        // 基础参数
        body.put("model", "speech-2.8-hd");
        body.put("text", request.getText());
        body.put("stream", Boolean.TRUE.equals(request.getStream()) || request.getStream() == null);
        body.put("output_format", request.getOutputFormat() != null ? request.getOutputFormat() : "hex");

        // voice_setting 音色设置
        Map<String, Object> voiceSetting = new HashMap<>();
        voiceSetting.put("voice_id", request.getVoiceId());
        voiceSetting.put("speed", request.getSpeed());
        voiceSetting.put("vol", request.getVol());
        voiceSetting.put("pitch", request.getPitch());
        voiceSetting.put("emotion", request.getEmotion());

        // 可选参数（避免传递null）
        if (Boolean.TRUE.equals(request.getTextNormalization())) {
            voiceSetting.put("text_normalization", true);
        }
        if (Boolean.TRUE.equals(request.getLatexRead())) {
            voiceSetting.put("latex_read", true);
        }
        body.put("voice_setting", voiceSetting);

        // audio_setting 音频设置
        Map<String, Object> audioSetting = new HashMap<>();
        audioSetting.put("sample_rate", request.getSampleRate());
        audioSetting.put("bitrate", request.getBitrate());
        audioSetting.put("format", request.getFormat());
        audioSetting.put("channel", request.getChannel());
        if (Boolean.TRUE.equals(request.getForceCbr())) {
            audioSetting.put("force_cbr", true);
        }
        body.put("audio_setting", audioSetting);

        // voice_modify 声音效果器（可选参数）
        Map<String, Object> voiceModify = new HashMap<>();
        boolean hasVoiceModify = false;

        if (request.getVoicePitch() != null) {
            voiceModify.put("pitch", request.getVoicePitch());
            hasVoiceModify = true;
        }
        if (request.getVoiceIntensity() != null) {
            voiceModify.put("intensity", request.getVoiceIntensity());
            hasVoiceModify = true;
        }
        if (request.getVoiceTimbre() != null) {
            voiceModify.put("timbre", request.getVoiceTimbre());
            hasVoiceModify = true;
        }
        if (request.getSoundEffects() != null && !request.getSoundEffects().isEmpty()) {
            voiceModify.put("sound_effects", request.getSoundEffects());
            hasVoiceModify = true;
        }
        if (hasVoiceModify) {
            body.put("voice_modify", voiceModify);
        }

        // 发音字典
        if (request.getPronunciationDict() != null && request.getPronunciationDict().length > 0) {
            body.put("pronunciation_dict", request.getPronunciationDict());
        }

        // 混合音色权重
        if (request.getTimbreWeights() != null && request.getTimbreWeights().length > 0) {
            body.put("timbre_weights", request.getTimbreWeights());
        }

        // 语言增强
        if (request.getLanguageBoost() != null && !request.getLanguageBoost().isEmpty()) {
            body.put("language_boost", request.getLanguageBoost());
        }

        // 字幕
        if (Boolean.TRUE.equals(request.getSubtitleEnable())) {
            body.put("subtitle_enable", true);
        }

        // AIGC水印
        if (Boolean.TRUE.equals(request.getAigcWatermark())) {
            body.put("aigc_watermark", true);
        }

        return body;
    }

    /**
     * 获取可用的音色列表
     *
     * @return 音色列表，包含系统音色、复刻音色和文生音色
     *
     * @apiNote 音色类型:
     * <ul>
     *   <li>system_voice - 系统预置音色</li>
     *   <li>voice_cloning - 复刻音色（需实名认证）</li>
     *   <li>voice_generation - 文生音色</li>
     * </ul>
     *
     * @apiNote 使用示例:
     * <pre>
     * GET /api/tts/voices
     *
     * Response:
     * {
     *   "code": 0,
     *   "msg": "success",
     *   "data": [
     *     {
     *       "voiceId": "male-qn-qingse",
     *       "voiceName": "清涩男声",
     *       "type": "system_voice",
     *       "description": ["年轻男性", "清澈"],
     *       "createdTime": "2024-01-01 00:00:00"
     *     }
     *   ]
     * }
     * </pre>
     */
    public List<VoiceResponse> getVoices() {
        Map<String, Object> body = Map.of("voice_type", "all");

        log.info("Fetching voice list from MiniMax API");

        Request request = new Request.Builder()
                .url(MINI_MAX_API_URL + "/get_voice")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .post(RequestBody.create(JSON.toJSONString(body), okhttp3.MediaType.parse("application/json; charset=utf-8")))
                .build();

        try {
            Map<String, Object> responseMap = OkHttpUtil.execute(request, Map.class);

            if (responseMap == null) {
                log.warn("Empty response from voice API");
                return List.of();
            }

            List<VoiceResponse> voices = new ArrayList<>();

            // 解析系统音色
            List<Map<String, Object>> systemVoices = (List<Map<String, Object>>) responseMap.get("system_voice");
            if (systemVoices != null) {
                for (Map<String, Object> voice : systemVoices) {
                    voices.add(parseVoice(voice, "system_voice"));
                }
            }

            // 解析复刻音色（需实名认证）
            List<Map<String, Object>> cloningVoices = (List<Map<String, Object>>) responseMap.get("voice_cloning");
            if (cloningVoices != null) {
                for (Map<String, Object> voice : cloningVoices) {
                    voices.add(parseVoice(voice, "voice_cloning"));
                }
            }

            // 解析文生音色
            List<Map<String, Object>> generationVoices = (List<Map<String, Object>>) responseMap.get("voice_generation");
            if (generationVoices != null) {
                for (Map<String, Object> voice : generationVoices) {
                    voices.add(parseVoice(voice, "voice_generation"));
                }
            }

            log.info("Fetched {} voices from API", voices.size());
            return voices;
        } catch (IOException e) {
            log.error("Error fetching voices from MiniMax API", e);
            return List.of();
        }
    }

    /**
     * 解析音色数据
     *
     * @param voice 音色原始数据
     * @param type 音色类型
     * @return VoiceResponse
     */
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
