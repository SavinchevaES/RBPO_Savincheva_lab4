package com.example.rbpo_savincheva.config;

import com.example.rbpo_savincheva.entity.User;
import com.example.rbpo_savincheva.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository users) {
    return email -> {
      User user = users.findByEmailIgnoreCase(email.trim())
          .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
      if (user.getPasswordHash() == null || user.getRole() == null) {
        throw new UsernameNotFoundException("Invalid credentials");
      }
      return new org.springframework.security.core.userdetails.User(
          user.getEmail(), user.getPasswordHash(), user.isEnabled(), true, true, true,
          java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    };
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
    CookieCsrfTokenRepository csrf = CookieCsrfTokenRepository.withHttpOnlyFalse();
    http
        .csrf(c -> c.csrfTokenRepository(csrf))
        .httpBasic(b -> {})
        .authorizeHttpRequests(a -> a
            .requestMatchers("/api/auth/register", "/api/auth/csrf", "/hello", "/number").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll())
        .exceptionHandling(e -> e
            .authenticationEntryPoint((request, response, ex) -> {
              response.setHeader("WWW-Authenticate", "Basic realm=\"Volunteer API\"");
              writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                  "Authentication is required");
            })
            .accessDeniedHandler((request, response, ex) -> writeError(response, objectMapper,
                HttpServletResponse.SC_FORBIDDEN, "Access is denied")));
    return http.build();
  }

  private static void writeError(HttpServletResponse response, ObjectMapper mapper, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getOutputStream(), Map.of("error", message));
  }
}
