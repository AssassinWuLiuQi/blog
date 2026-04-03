package com.scholarsmanuscript.service;

import com.alibaba.fastjson2.JSON;
import com.scholarsmanuscript.dto.request.ImageGenerationRequest;
import com.scholarsmanuscript.dto.response.ImageGenerationHistoryResponse;
import com.scholarsmanuscript.dto.response.ImageGenerationResponse;
import com.scholarsmanuscript.entity.ImageGenerationHistory;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.ImageGenerationHistoryRepository;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationHistoryService {

    private final ImageGenerationHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional
    public ImageGenerationResponse saveHistory(ImageGenerationRequest request, ImageGenerationResponse response) {
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

        return response;
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
