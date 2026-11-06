package com.example.rbpo_savincheva.service;

import com.example.rbpo_savincheva.repository.UserRepository;
import com.example.rbpo_savincheva.repository.VolunteerCampaignRepository;
import com.example.rbpo_savincheva.repository.VolunteerResponseRepository;
import com.example.rbpo_savincheva.repository.VolunteerTaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authorizationService")
public class AuthorizationService {
  private final UserRepository users;
  private final VolunteerCampaignRepository campaigns;
  private final VolunteerTaskRepository tasks;
  private final VolunteerResponseRepository responses;

  public AuthorizationService(UserRepository users, VolunteerCampaignRepository campaigns,
      VolunteerTaskRepository tasks, VolunteerResponseRepository responses) {
    this.users = users;
    this.campaigns = campaigns;
    this.tasks = tasks;
    this.responses = responses;
  }

  public boolean isCurrentUser(Long userId) {
    if (userId == null) return false;
    return users.findByEmailIgnoreCase(currentEmail()).map(u -> u.getId().equals(userId)).orElse(false);
  }

  public boolean isCampaignCoordinator(Long campaignId) {
    return campaigns.findById(campaignId)
        .map(c -> isCurrentUser(c.getCoordinator().getId())).orElse(false);
  }

  public boolean isTaskCoordinator(Long taskId) {
    return tasks.findById(taskId)
        .map(t -> isCurrentUser(t.getCampaign().getCoordinator().getId())).orElse(false);
  }

  public boolean isResponseCoordinator(Long responseId) {
    return responses.findById(responseId)
        .map(r -> isCurrentUser(r.getTask().getCampaign().getCoordinator().getId())).orElse(false);
  }

  public boolean isResponseVolunteer(Long responseId) {
    return responses.findById(responseId)
        .map(r -> isCurrentUser(r.getVolunteer().getId())).orElse(false);
  }

  private String currentEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth == null ? "" : auth.getName();
  }
}
