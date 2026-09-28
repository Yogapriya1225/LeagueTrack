package com.example.leaguetrack.service;

import com.example.leaguetrack.config.ScoringConfig;
import com.example.leaguetrack.dto.ScoringRuleDto;
import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.MatchStatus;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.StandingsEntryRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StandingsService {

    private final StandingsEntryRepository standingsEntryRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final ScoringConfig scoringConfig;
    private final NotificationService notificationService;

    public StandingsService(StandingsEntryRepository standingsEntryRepository,
                            TeamRepository teamRepository,
                            MatchRepository matchRepository,
                            ScoringConfig scoringConfig,
                            NotificationService notificationService) {
        this.standingsEntryRepository = standingsEntryRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.scoringConfig = scoringConfig;
        this.notificationService = notificationService;
    }

    public List<StandingsEntry> getStandings() {
        return standingsEntryRepository.findAllByOrderByPointsDescGoalDifferenceDescGoalsForDescWonDescTeam_NameAsc();
    }

    public ScoringRuleDto getScoringRule() {
        return new ScoringRuleDto(
                scoringConfig.getWinPoints(),
                scoringConfig.getDrawPoints(),
                scoringConfig.getLossPoints(),
                String.format("Win: %d pts, Draw: %d pts, Loss: %d pts",
                        scoringConfig.getWinPoints(),
                        scoringConfig.getDrawPoints(),
                        scoringConfig.getLossPoints())
        );
    }

    @Transactional
    public ScoringRuleDto updateScoringRule(int win, int draw, int loss) {
        scoringConfig.setWinPoints(win);
        scoringConfig.setDrawPoints(draw);
        scoringConfig.setLossPoints(loss);
        recalculateStandings();
        notificationService.notify("SCORING_RULE_UPDATED",
                String.format("Scoring rules updated to Win=%d, Draw=%d, Loss=%d and standings recalculated", win, draw, loss));
        return getScoringRule();
    }

    @Transactional
    public List<StandingsEntry> recalculateStandings() {
        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            StandingsEntry entry = standingsEntryRepository.findByTeam_Id(team.getId())
                    .orElseGet(() -> new StandingsEntry(team));
            entry.reset();
            standingsEntryRepository.save(entry);
        }

        List<Match> completedMatches = matchRepository.findByStatus(MatchStatus.COMPLETED);
        int winPts = scoringConfig.getWinPoints();
        int drawPts = scoringConfig.getDrawPoints();
        int lossPts = scoringConfig.getLossPoints();

        for (Match match : completedMatches) {
            if (match.getHomeScore() == null || match.getAwayScore() == null) continue;

            StandingsEntry homeEntry = standingsEntryRepository.findByTeam_Id(match.getHomeTeam().getId())
                    .orElseGet(() -> new StandingsEntry(match.getHomeTeam()));
            StandingsEntry awayEntry = standingsEntryRepository.findByTeam_Id(match.getAwayTeam().getId())
                    .orElseGet(() -> new StandingsEntry(match.getAwayTeam()));

            homeEntry.applyResult(match.getHomeScore(), match.getAwayScore(), winPts, drawPts, lossPts);
            awayEntry.applyResult(match.getAwayScore(), match.getHomeScore(), winPts, drawPts, lossPts);

            standingsEntryRepository.save(homeEntry);
            standingsEntryRepository.save(awayEntry);
        }

        notificationService.notify("STANDINGS_RECALCULATED", "Standings table recalculated successfully");
        return getStandings();
    }

    @Transactional
    public void resetStandings() {
        List<StandingsEntry> entries = standingsEntryRepository.findAll();
        for (StandingsEntry entry : entries) {
            entry.reset();
        }
        standingsEntryRepository.saveAll(entries);
        notificationService.notify("STANDINGS_RESET", "All standings stats reset to zero");
    }
}
