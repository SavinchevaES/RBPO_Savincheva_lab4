package com.example.rbpo_savincheva.controller;

import com.example.rbpo_savincheva.dto.Requests.ResponseRequest;
import com.example.rbpo_savincheva.dto.Responses.VolunteerResponseDto;
import com.example.rbpo_savincheva.dto.Requests.SubmitReportRequest;
import com.example.rbpo_savincheva.dto.Responses.ReportDto;
import com.example.rbpo_savincheva.service.ParticipationReportService;
import com.example.rbpo_savincheva.service.VolunteerResponseService;
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
@RequestMapping("/api/volunteer-responses")
@PreAuthorize("isAuthenticated()")
public class VolunteerResponseController {
  private final VolunteerResponseService service;
  private final ParticipationReportService reportService;

  public VolunteerResponseController(VolunteerResponseService service, ParticipationReportService reportService) {
    this.service = service;
    this.reportService = reportService;
  }

  @GetMapping
  public List<VolunteerResponseDto> all() {
    return service.findAll().stream().map(VolunteerResponseDto::from).toList();
  }

  @GetMapping("/{id}")
  public VolunteerResponseDto one(@PathVariable long id) {
    return VolunteerResponseDto.from(service.find(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VolunteerResponseDto> create(@RequestBody ResponseRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(VolunteerResponseDto.from(service.create(request)));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public VolunteerResponseDto patch(@PathVariable long id, @RequestBody ResponseRequest request) {
    return VolunteerResponseDto.from(service.patch(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    service.delete(id);
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("hasRole('ADMIN') or (hasRole('COORDINATOR') and @authorizationService.isResponseCoordinator(#id))")
  public VolunteerResponseDto approve(@PathVariable long id) {
    return VolunteerResponseDto.from(service.approve(id));
  }

  @PostMapping("/{id}/report")
  @PreAuthorize("hasRole('VOLUNTEER') and @authorizationService.isResponseVolunteer(#id)")
  public ResponseEntity<ReportDto> report(
      @PathVariable long id, @RequestBody SubmitReportRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ReportDto.from(reportService.submit(id, request)));
  }
}
