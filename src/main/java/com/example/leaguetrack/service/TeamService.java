package com.example.leaguetrack.service;

import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    public TeamService(TeamRepository teamRepository, MatchRepository matchRepository) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found with id: " + id));
    }

    @Transactional
    public Team registerTeam(Team team) {
        if (team.getName() == null || team.getName().isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }

        team.setName(team.getName().trim());

        if (teamRepository.existsByNameIgnoreCase(team.getName())) {
            throw new IllegalArgumentException("A team with this name already exists");
        }

        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        getTeam(id);
        matchRepository.deleteAllByHomeTeam_IdOrAwayTeam_Id(id, id);
        teamRepository.deleteById(id);
    }
}
