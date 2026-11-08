package com.sizeok.profile.controller;

import com.sizeok.profile.controller.ProfileDtos.CreateProfileRequest;
import com.sizeok.profile.controller.ProfileDtos.ProfileResponse;
import com.sizeok.profile.controller.ProfileDtos.UpdateProfileRequest;
import com.sizeok.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return profileService.createProfile(request);
    }

    @GetMapping
    public List<ProfileResponse> listProfiles() {
        return profileService.listMyProfiles();
    }

    @GetMapping("/{id}")
    public ProfileResponse getProfile(@PathVariable("id") String id) {
        return profileService.getProfile(id);
    }

    @PatchMapping("/{id}")
    public ProfileResponse updateProfile(
            @PathVariable("id") String id,
            @RequestBody UpdateProfileRequest request
    ) {
        return profileService.updateProfile(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable("id") String id) {
        profileService.deleteProfile(id);
    }
}
