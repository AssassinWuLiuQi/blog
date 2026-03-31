package com.scholarsmanuscript.controller;

import com.scholarsmanuscript.dto.request.PasswordChangeRequest;
import com.scholarsmanuscript.dto.request.UserSettingsRequest;
import com.scholarsmanuscript.dto.response.ApiResponse;
import com.scholarsmanuscript.dto.response.UserSettingsResponse;
import com.scholarsmanuscript.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings() {
        UserSettingsResponse settings = userService.getUserSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateUserSettings(
            @RequestBody UserSettingsRequest request) {
        UserSettingsResponse settings = userService.updateUserSettings(request);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
