package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.UserSettingsRequest;
import com.scholarsmanuscript.dto.response.UserSettingsResponse;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserSettingsResponse getUserSettings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return mapToUserSettingsResponse(user);
    }

    @Transactional
    public UserSettingsResponse updateUserSettings(UserSettingsRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getTheme() != null) {
            user.setTheme(request.getTheme());
        }
        if (request.getFont() != null) {
            user.setFont(request.getFont());
        }
        if (request.getVoiceSetting() != null) {
            user.setVoiceSetting(request.getVoiceSetting());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserSettingsResponse(updatedUser);
    }

    private UserSettingsResponse mapToUserSettingsResponse(User user) {
        return UserSettingsResponse.builder()
                .theme(user.getTheme())
                .font(user.getFont())
                .voiceSetting(user.getVoiceSetting())
                .build();
    }
}
