package com.sizeok.identity.controller;

import com.sizeok.identity.controller.IdentityDtos.DeletionRequestResponse;
import com.sizeok.identity.controller.IdentityDtos.PrivacySettingsResponse;
import com.sizeok.identity.controller.IdentityDtos.UpdatePrivacySettingsRequest;
import com.sizeok.identity.controller.IdentityDtos.UpdateUserRequest;
import com.sizeok.identity.controller.IdentityDtos.UserDataExportResponse;
import com.sizeok.identity.controller.IdentityDtos.UserResponse;
import com.sizeok.identity.service.IdentityService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {

    private final IdentityService identityService;

    public UserController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping
    public UserResponse getMe() {
        return identityService.getMe();
    }

    @PatchMapping
    public UserResponse updateMe(@RequestBody UpdateUserRequest request) {
        return identityService.updateMe(request);
    }

    @DeleteMapping
    public DeletionRequestResponse deleteMe() {
        return identityService.initiateDeletion();
    }

    @PostMapping("/deletion-request")
    public DeletionRequestResponse requestDeletion() {
        return identityService.initiateDeletion();
    }

    @GetMapping("/privacy")
    public PrivacySettingsResponse getPrivacy() {
        return identityService.getPrivacySettings();
    }

    @PatchMapping("/privacy")
    public PrivacySettingsResponse updatePrivacy(@RequestBody UpdatePrivacySettingsRequest request) {
        return identityService.updatePrivacySettings(request);
    }

    @GetMapping("/export")
    public UserDataExportResponse exportData() {
        return identityService.exportUserData();
    }
}
