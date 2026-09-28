package com.example.leaguetrack.service;

import com.example.leaguetrack.exception.BusinessRuleException;
import com.example.leaguetrack.exception.ResourceNotFoundException;
import com.example.leaguetrack.model.Fixture;
import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.repository.FixtureRepository;
import com.example.leaguetrack.repository.MatchRepository;
import com.example.leaguetrack.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final StandingsService standingsService;
    private final NotificationService notificationService;

    public FixtureService(FixtureRepository fixtureRepository,
                          MatchRepository matchRepository,
                          TeamRepository teamRepository,
                          StandingsService standingsService,
                          NotificationService notificationService) {
        this.fixtureRepository = fixtureRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.standingsService = standingsService;
        this.notificationService = notificationService;
    }

    public List<Fixture> getAllFixtures() {
        return fixtureRepository.findAllByOrderByRoundNumberAsc();
    }

    public Fixture getFixtureByRound(Integer roundNumber) {
        return fixtureRepository.findByRoundNumber(roundNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture for round " + roundNumber + " not found"));
    }

    @Transactional
    public List<Fixture> generateRoundRobinFixtures() {
        List<Team> teams = new ArrayList<>(teamRepository.findAll());

        if (teams.size() < 2) {
            throw new BusinessRuleException("At least 2 registered teams are required to generate tournament fixtures. Current teams: " + teams.size());
        }

        // Clean previous tournament matches and fixtures
        matchRepository.deleteAll();
        fixtureRepository.deleteAll();

        // Reset standings table for all teams
        standingsService.resetStandings();

        boolean hasBye = (teams.size() % 2 != 0);
        if (hasBye) {
            teams.add(null); // Dummy team for BYE
        }

        int totalTeams = teams.size();
        int totalRounds = totalTeams - 1;
        int matchesPerRound = totalTeams / 2;

        List<Fixture> savedFixtures = new ArrayList<>();
        String[] venues = {"Main Sports Arena", "Court A", "Court B", "Turf Field", "Indoor Stadium"};

        for (int round = 1; round <= totalRounds; round++) {
            Fixture fixture = new Fixture(round, "Round " + round, "Intramural Tournament Round " + round);
            fixture = fixtureRepository.save(fixture);

            int venueIdx = 0;
            for (int i = 0; i < matchesPerRound; i++) {
                Team first = teams.get(i);
                Team second = teams.get(totalTeams - 1 - i);

                if (first != null && second != null) {
                    Team homeTeam = (round % 2 == 1) ? first : second;
                    Team awayTeam = (round % 2 == 1) ? second : first;
                    String venue = venues[venueIdx % venues.length];
                    venueIdx++;

                    Match match = new Match(fixture, homeTeam, awayTeam, round, venue);
                    fixture.addMatch(match);
                }
            }

            matchRepository.saveAll(fixture.getMatches());
            savedFixtures.add(fixture);

            // Circle rotation algorithm: keep first element fixed, rotate remaining elements
            List<Team> rotated = new ArrayList<>();
            rotated.add(teams.get(0));
            rotated.add(teams.get(totalTeams - 1));

            for (int i = 1; i < totalTeams - 1; i++) {
                rotated.add(teams.get(i));
            }
            teams = rotated;
        }

        int totalMatches = savedFixtures.stream().mapToInt(f -> f.getMatches().size()).sum();
        notificationService.notify("FIXTURES_GENERATED",
                String.format("Generated %d fixtures across %d rounds for %d teams",
                        totalMatches, totalRounds, teamRepository.count()));

        return savedFixtures;
    }
}
