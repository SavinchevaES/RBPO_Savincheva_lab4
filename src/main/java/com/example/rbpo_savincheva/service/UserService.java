package com.example.rbpo_savincheva.service;

import com.example.rbpo_savincheva.dto.Requests.UserRequest;
import com.example.rbpo_savincheva.dto.Requests.RegistrationRequest;
import com.example.rbpo_savincheva.entity.Role;
import com.example.rbpo_savincheva.entity.User;
import com.example.rbpo_savincheva.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@Transactional
public class UserService {
  private final UserRepository repository;
  private final VolunteerCleanupService cleanup;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository repository, VolunteerCleanupService cleanup, PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.cleanup = cleanup;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> findAll() {
    return repository.findAll();
  }

  public User find(long id) {
    return repository.findById(id).orElseThrow(() -> new NotFoundException("User", id));
  }

  public User create(UserRequest r) {
    validate(r.name(), r.email(), r.password());
    User user = new User();
    user.setName(r.name());
    user.setEmail(normalizeEmail(r.email()));
    user.setPasswordHash(passwordEncoder.encode(r.password()));
    user.setRole(Role.VOLUNTEER);
    user.setEnabled(true);
    return repository.saveAndFlush(user);
  }

  public User register(RegistrationRequest r) {
    if (r == null) throw new IllegalArgumentException("Registration data is required");
    validate(r.name(), r.email(), r.password());
    String email = normalizeEmail(r.email());
    if (repository.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("An account with this email already exists");
    }
    User user = new User();
    user.setName(r.name().trim());
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(r.password()));
    user.setRole(Role.VOLUNTEER);
    user.setEnabled(true);
    return repository.saveAndFlush(user);
  }

  public User patch(long id, UserRequest r) {
    User user = find(id);
    String name = r.name() == null ? user.getName() : r.name();
    String email = r.email() == null ? user.getEmail() : r.email();
    if (name == null || name.isBlank() || email == null || email.isBlank())
      throw new IllegalArgumentException("name and email are required");
    String normalizedEmail = normalizeEmail(email);
    repository.findByEmailIgnoreCase(normalizedEmail)
        .filter(other -> !other.getId().equals(id))
        .ifPresent(other -> { throw new ConflictException("An account with this email already exists"); });
    user.setName(name);
    user.setEmail(normalizedEmail);
    return repository.saveAndFlush(user);
  }

  public void delete(long id) {
    find(id);
    cleanup.deleteUser(id);
  }

  private void validate(String name, String email, String password) {
    if (name == null || name.isBlank() || email == null || email.isBlank())
      throw new IllegalArgumentException("name and email are required");
    String normalizedEmail = normalizeEmail(email);
    if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
      throw new IllegalArgumentException("email must be valid");
    if (repository.existsByEmailIgnoreCase(normalizedEmail))
      throw new ConflictException("An account with this email already exists");
    if (password == null || password.length() < 8 || !password.matches(".*[A-Z].*")
        || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")
        || !password.matches(".*[^A-Za-z0-9].*"))
      throw new IllegalArgumentException(
          "password must be at least 8 characters and contain upper-case, lower-case, digit and special character");
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(java.util.Locale.ROOT);
  }
}
