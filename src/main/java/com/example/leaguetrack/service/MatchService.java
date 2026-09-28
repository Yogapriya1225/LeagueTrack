package com.example.leaguetrack.service;

import com.example.leaguetrack.config.ScoringConfig;
import com.example.leaguetrack.dto.MatchResultRequest;
import com.example.leaguetrack.exception.BusinessRuleException;
import com.example.leaguetrack.exception.ResourceNotFoundException;
import com.example.leaguetrack.model.Fixture;
import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.MatchStatus;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.repository.FixtureRepository;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.StandingsEntryRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;
    private final StandingsEntryRepository standingsEntryRepository;
    private final FixtureService fixtureService;
    private final ScoringConfig scoringConfig;
    private final NotificationService notificationService;

    public MatchService(MatchRepository matchRepository,
                        FixtureRepository fixtureRepository,
                        TeamRepository teamRepository,
                        StandingsEntryRepository standingsEntryRepository,
                        FixtureService fixtureService,
                        ScoringConfig scoringConfig,
                        NotificationService notificationService) {
        this.matchRepository = matchRepository;
        this.fixtureRepository = fixtureRepository;
        this.teamRepository = teamRepository;
        this.standingsEntryRepository = standingsEntryRepository;
        this.fixtureService = fixtureService;
        this.scoringConfig = scoringConfig;
        this.notificationService = notificationService;
    }

    public List<Match> getAllMatches() {
        return matchRepository.findAllByOrderByRoundNumberAscIdAsc();
    }

    public Page<Match> getAllMatches(Pageable pageable) {
        return matchRepository.findAll(pageable);
    }

    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
    }

    @Transactional
    public List<Match> generateFixtures() {
        fixtureService.generateRoundRobinFixtures();
        return getAllMatches();
    }

    @Transactional
    public Match recordResult(Long matchId, MatchResultRequest request) {
        if (request == null) {
            throw new BusinessRuleException("Match result payload cannot be null");
        }
        return recordResult(matchId, request.getHomeScore(), request.getAwayScore());
    }

    @Transactional
    public Match recordResult(Long matchId, Integer homeScore, Integer awayScore) {
        if (homeScore == null || awayScore == null) {
            throw new BusinessRuleException("Both home and away scores must be provided");
        }

        if (homeScore < 0 || awayScore < 0) {
            throw new BusinessRuleException("Scores cannot be negative. Provided: home=" + homeScore + ", away=" + awayScore);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        int winPts = scoringConfig.getWinPoints();
        int drawPts = scoringConfig.getDrawPoints();
        int lossPts = scoringConfig.getLossPoints();

        StandingsEntry homeStanding = standingsEntryRepository.findByTeam_Id(match.getHomeTeam().getId())
                .orElseGet(() -> standingsEntryRepository.save(new StandingsEntry(match.getHomeTeam())));

        StandingsEntry awayStanding = standingsEntryRepository.findByTeam_Id(match.getAwayTeam().getId())
                .orElseGet(() -> standingsEntryRepository.save(new StandingsEntry(match.getAwayTeam())));

        // Enforce Business Rule:
        // "A match result, once recorded, updates standings exactly once (no duplicate counting on edit without adjustment)."
        if (match.getStatus() == MatchStatus.COMPLETED && match.getHomeScore() != null && match.getAwayScore() != null) {
            homeStanding.revertResult(match.getHomeScore(), match.getAwayScore(), winPts, drawPts, lossPts);
            awayStanding.revertResult(match.getAwayScore(), match.getHomeScore(), winPts, drawPts, lossPts);
            notificationService.notify("MATCH_RESULT_ADJUSTED",
                    String.format("Previous result (%d-%d) rolled back for match %s vs %s before applying edit",
                            match.getHomeScore(), match.getAwayScore(),
                            match.getHomeTeam().getName(), match.getAwayTeam().getName()));
        }

        // Apply new result to both standings
        homeStanding.applyResult(homeScore, awayScore, winPts, drawPts, lossPts);
        awayStanding.applyResult(awayScore, homeScore, winPts, drawPts, lossPts);

        standingsEntryRepository.save(homeStanding);
        standingsEntryRepository.save(awayStanding);

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setStatus(MatchStatus.COMPLETED);
        match.setCompletedAt(LocalDateTime.now());

        Match updatedMatch = matchRepository.save(match);

        notificationService.notify("MATCH_COMPLETED",
                String.format("Match #%d (Round %d): %s %d - %d %s | Status: COMPLETED",
                        updatedMatch.getId(),
                        updatedMatch.getRoundNumber(),
                        updatedMatch.getHomeTeam().getName(),
                        homeScore,
                        awayScore,
                        updatedMatch.getAwayTeam().getName()));

        return updatedMatch;
    }

    public List<StandingsEntry> getStandings() {
        return standingsEntryRepository.findAllByOrderByPointsDescGoalDifferenceDescGoalsForDescWonDescTeam_NameAsc();
    }

    @Transactional
    public void resetTournament() {
        matchRepository.deleteAll();
        fixtureRepository.deleteAll();
        standingsEntryRepository.deleteAll();
        teamRepository.deleteAll();
        notificationService.notify("TOURNAMENT_RESET", "All tournament data (teams, fixtures, matches, standings) has been reset.");
    }

    @Transactional
    public void resetMatchesOnly() {
        matchRepository.deleteAll();
        fixtureRepository.deleteAll();
        List<StandingsEntry> entries = standingsEntryRepository.findAll();
        for (StandingsEntry entry : entries) {
            entry.reset();
        }
        standingsEntryRepository.saveAll(entries);
        notificationService.notify("MATCHES_RESET", "Matches and fixtures cleared. Teams preserved and standings reset to 0.");
    }
}
