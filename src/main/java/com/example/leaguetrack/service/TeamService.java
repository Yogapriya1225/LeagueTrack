package com.example.leaguetrack.service;

import com.example.leaguetrack.dto.TeamRequest;
import com.example.leaguetrack.exception.BusinessRuleException;
import com.example.leaguetrack.exception.ResourceNotFoundException;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.StandingsEntryRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StandingsEntryRepository standingsEntryRepository;
    private final NotificationService notificationService;

    public TeamService(TeamRepository teamRepository,
                       MatchRepository matchRepository,
                       StandingsEntryRepository standingsEntryRepository,
                       NotificationService notificationService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.standingsEntryRepository = standingsEntryRepository;
        this.notificationService = notificationService;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Page<Team> getAllTeams(Pageable pageable) {
        return teamRepository.findAll(pageable);
    }

    public Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
    }

    @Transactional
    public Team registerTeam(TeamRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BusinessRuleException("Team name cannot be empty");
        }

        String trimmedName = request.getName().trim();
        if (teamRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BusinessRuleException("A team with name '" + trimmedName + "' is already registered");
        }

        Team team = new Team(trimmedName, request.getCaptain(), request.getDepartment(), request.getContactNumber());
        Team savedTeam = teamRepository.save(team);

        // Auto-initialize StandingsEntry in database for newly registered team
        StandingsEntry initialStanding = new StandingsEntry(savedTeam);
        standingsEntryRepository.save(initialStanding);

        notificationService.notify("TEAM_REGISTERED",
                "Team '" + savedTeam.getName() + "' successfully registered (ID: " + savedTeam.getId() + ")");

        return savedTeam;
    }

    @Transactional
    public Team updateTeam(Long id, TeamRequest request) {
        Team existing = getTeam(id);
        String trimmedName = request.getName() != null ? request.getName().trim() : "";

        if (trimmedName.isBlank()) {
            throw new BusinessRuleException("Team name cannot be blank");
        }

        if (!existing.getName().equalsIgnoreCase(trimmedName) && teamRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BusinessRuleException("A team with name '" + trimmedName + "' already exists");
        }

        existing.setName(trimmedName);
        if (request.getCaptain() != null) {
            existing.setCaptain(request.getCaptain().trim());
        }
        if (request.getDepartment() != null) {
            existing.setDepartment(request.getDepartment().trim());
        }
        if (request.getContactNumber() != null) {
            existing.setContactNumber(request.getContactNumber().trim());
        }

        Team updated = teamRepository.save(existing);
        notificationService.notify("TEAM_UPDATED", "Team '" + updated.getName() + "' details updated");
        return updated;
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = getTeam(id);
        matchRepository.deleteAllByHomeTeam_IdOrAwayTeam_Id(id, id);
        standingsEntryRepository.deleteByTeam_Id(id);
        teamRepository.deleteById(id);

        notificationService.notify("TEAM_DELETED",
                "Team '" + team.getName() + "' and its associated records were removed");
    }
}
