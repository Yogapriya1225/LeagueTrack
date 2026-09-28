package com.example.leaguetrack.service;

import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.MatchStatus;
import com.example.leaguetrack.model.Standing;
import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MatchService {
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public MatchService(MatchRepository matchRepository, TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByRoundNumberAscIdAsc();
    }

    @Transactional
    public List<Match> generateFixtures() {
        List<Team> teams = new ArrayList<>(teamRepository.findAll());

        if (teams.size() < 2) {
            throw new IllegalArgumentException("Register at least 2 teams before generating fixtures");
        }

        matchRepository.deleteAll();

        if (teams.size() % 2 != 0) {
            teams.add(null);
        }

        int totalTeams = teams.size();
        int rounds = totalTeams - 1;
        int matchesPerRound = totalTeams / 2;
        List<Match> fixtures = new ArrayList<>();

        for (int round = 1; round <= rounds; round++) {
            for (int i = 0; i < matchesPerRound; i++) {
                Team first = teams.get(i);
                Team second = teams.get(totalTeams - 1 - i);

                if (first != null && second != null) {
                    fixtures.add(new Match(first, second, round));
                }
            }

            List<Team> rotated = new ArrayList<>();
            rotated.add(teams.get(0));
            rotated.add(teams.get(totalTeams - 1));

            for (int i = 1; i < totalTeams - 1; i++) {
                rotated.add(teams.get(i));
            }

            teams = rotated;
        }

        return matchRepository.saveAll(fixtures);
    }

    @Transactional
    public Match recordResult(Long matchId, Integer homeScore, Integer awayScore) {
        if (homeScore == null || awayScore == null) {
            throw new IllegalArgumentException("Both scores are required");
        }

        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Scores cannot be negative");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("This match result has already been recorded");
        }

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setStatus(MatchStatus.COMPLETED);

        return matchRepository.save(match);
    }

    public List<Standing> getStandings() {
        List<Team> teams = teamRepository.findAll();
        List<Match> matches = matchRepository.findAll();

        Map<Long, Standing> table = new HashMap<>();

        for (Team team : teams) {
            table.put(team.getId(), new Standing(team.getId(), team.getName()));
        }

        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.COMPLETED) {
                continue;
            }

            Standing home = table.get(match.getHomeTeam().getId());
            Standing away = table.get(match.getAwayTeam().getId());

            if (match.getHomeScore() > match.getAwayScore()) {
                home.recordWin();
                away.recordLoss();
            } else if (match.getHomeScore() < match.getAwayScore()) {
                home.recordLoss();
                away.recordWin();
            } else {
                home.recordDraw();
                away.recordDraw();
            }
        }

        List<Standing> standings = new ArrayList<>(table.values());

        standings.sort(
                Comparator.comparingInt(Standing::getPoints).reversed()
                        .thenComparing(Comparator.comparingInt(Standing::getWins).reversed())
                        .thenComparing(Standing::getTeamName, String.CASE_INSENSITIVE_ORDER)
        );

        return standings;
    }

    @Transactional
    public void resetTournament() {
        matchRepository.deleteAll();
        teamRepository.deleteAll();
    }
}
