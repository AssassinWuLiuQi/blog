package com.scholarsmanuscript.service;

import com.scholarsmanuscript.dto.request.PasswordChangeRequest;
import com.scholarsmanuscript.dto.request.UserSettingsRequest;
import com.scholarsmanuscript.dto.response.UserSettingsResponse;
import com.scholarsmanuscript.entity.User;
import com.scholarsmanuscript.exception.BusinessException;
import com.scholarsmanuscript.exception.ErrorCode;
import com.scholarsmanuscript.repository.UserRepository;
import com.scholarsmanuscript.utils.RsaEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RsaEncryptor rsaEncryptor;

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

    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Decrypt RSA encrypted passwords from request
        String oldPasswordDecrypted = rsaEncryptor.decrypt(request.getOldPassword());
        String newPasswordDecrypted = rsaEncryptor.decrypt(request.getNewPassword());

        // Verify old password
        if (!passwordEncoder.matches(oldPasswordDecrypted, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // Update to new password (BCrypt encoded)
        user.setPassword(passwordEncoder.encode(newPasswordDecrypted));
        userRepository.save(user);
    }

    private UserSettingsResponse mapToUserSettingsResponse(User user) {
        return UserSettingsResponse.builder()
                .theme(user.getTheme())
                .font(user.getFont())
                .voiceSetting(user.getVoiceSetting())
                .build();
    }
}
