package com.example.rbpo_savincheva.controller;

import com.example.rbpo_savincheva.dto.Requests.RegistrationRequest;
import com.example.rbpo_savincheva.dto.Responses.UserDto;
import com.example.rbpo_savincheva.service.UserService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserService users;

  public AuthController(UserService users) { this.users = users; }

  @PostMapping("/register")
  public ResponseEntity<UserDto> register(@RequestBody RegistrationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(users.register(request)));
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
  }
}
