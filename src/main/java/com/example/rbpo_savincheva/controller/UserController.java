package com.example.rbpo_savincheva.controller;

import com.example.rbpo_savincheva.dto.Requests.UserRequest;
import com.example.rbpo_savincheva.dto.Responses.UserDto;
import com.example.rbpo_savincheva.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserDto> all() {
    return service.findAll().stream().map(UserDto::from).toList();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @authorizationService.isCurrentUser(#id)")
  public UserDto one(@PathVariable long id) {
    return UserDto.from(service.find(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> create(@RequestBody UserRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(service.create(request)));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or @authorizationService.isCurrentUser(#id)")
  public UserDto patch(@PathVariable long id, @RequestBody UserRequest request) {
    return UserDto.from(service.patch(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    service.delete(id);
  }
}
