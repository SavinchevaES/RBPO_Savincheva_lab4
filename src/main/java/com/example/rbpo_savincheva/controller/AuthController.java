package com.example.rbpo_savincheva.controller;

import com.example.rbpo_savincheva.dto.Requests.RegistrationRequest;
import com.example.rbpo_savincheva.dto.Requests.LoginRequest;
import com.example.rbpo_savincheva.dto.Requests.RefreshRequest;
import com.example.rbpo_savincheva.dto.Responses.TokenPairDto;
import com.example.rbpo_savincheva.service.AuthenticationService;
import com.example.rbpo_savincheva.service.TokenPairService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {
  private final AuthenticationService authentication;
  private final TokenPairService tokens;

  public AuthController(AuthenticationService authentication, TokenPairService tokens) {
    this.authentication = authentication;
    this.tokens = tokens;
  }

  @PostMapping("/register")
  public ResponseEntity<TokenPairDto> register(@RequestBody RegistrationRequest request,
      @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authentication.register(request, deviceId));
  }

  @PostMapping("/login")
  public TokenPairDto login(@RequestBody LoginRequest request,
      @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
    return authentication.login(request, deviceId);
  }

  @PostMapping("/refresh")
  public TokenPairDto refresh(@RequestBody RefreshRequest request) {
    if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
      throw new IllegalArgumentException("refreshToken is required");
    }
    return tokens.refresh(request.refreshToken());
  }
}
