package com.sizeok.identity.controller;

import com.sizeok.identity.controller.IdentityDtos.AuthProviderInfo;
import com.sizeok.identity.controller.IdentityDtos.AuthResponse;
import com.sizeok.identity.controller.IdentityDtos.LoginRequest;
import com.sizeok.identity.controller.IdentityDtos.OAuthLoginRequest;
import com.sizeok.identity.controller.IdentityDtos.RegisterRequest;
import com.sizeok.identity.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IdentityService identityService;

    public AuthController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping("/providers")
    public List<AuthProviderInfo> providers() {
        return identityService.getSupportedProviders();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return identityService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return identityService.login(request);
    }

    @PostMapping("/oauth")
    public AuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        return identityService.oauthLogin(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        identityService.logout();
    }
}
