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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .csrf(c -> c.disable())
        .httpBasic(b -> b.disable())
        .formLogin(f -> f.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(a -> a
            .requestMatchers(HttpMethod.POST,
                "/api/auth/login", "/api/auth/register", "/api/auth/refresh",
                "/auth/login", "/auth/register", "/auth/refresh").permitAll()
            .requestMatchers("/hello", "/number").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().denyAll())
        .exceptionHandling(e -> e
            .authenticationEntryPoint((request, response, ex) -> {
              response.setHeader("WWW-Authenticate", "Bearer");
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
