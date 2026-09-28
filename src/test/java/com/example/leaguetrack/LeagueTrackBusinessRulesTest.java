package com.example.leaguetrack;

import com.example.leaguetrack.dto.MatchResultRequest;
import com.example.leaguetrack.dto.TeamRequest;
import com.example.leaguetrack.exception.BusinessRuleException;
import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.MatchStatus;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.model.Team;
import com.example.leaguetrack.service.FixtureService;
import com.example.leaguetrack.service.MatchService;
import com.example.leaguetrack.service.StandingsService;
import com.example.leaguetrack.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LeagueTrackBusinessRulesTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private FixtureService fixtureService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private StandingsService standingsService;

    @BeforeEach
    void setup() {
        matchService.resetTournament();
    }

    @Test
    @DisplayName("Rule 1: Register teams initializes standings entry with 0 stats")
    void testRegisterTeamInitializesStandings() {
        Team t1 = teamService.registerTeam(new TeamRequest("CSE Titans", "Alice", "Computer Science", "9876543210"));
        Team t2 = teamService.registerTeam(new TeamRequest("ECE Warriors", "Bob", "Electronics", "9876543211"));

        assertNotNull(t1.getId());
        assertNotNull(t2.getId());

        List<StandingsEntry> standings = standingsService.getStandings();
        assertEquals(2, standings.size());
        assertEquals(0, standings.get(0).getPlayed());
        assertEquals(0, standings.get(0).getPoints());
    }

    @Test
    @DisplayName("Rule 2: Duplicate team name is rejected with BusinessRuleException")
    void testDuplicateTeamNameRejected() {
        teamService.registerTeam(new TeamRequest("Mechanical Bulls", "Charlie", "Mech", "123"));

        assertThrows(BusinessRuleException.class, () -> {
            teamService.registerTeam(new TeamRequest("mechanical bulls", "David", "Mech", "456"));
        });
    }

    @Test
    @DisplayName("Rule 3: Auto-generate round-robin fixtures requires at least 2 teams")
    void testFixtureGenerationValidation() {
        assertThrows(BusinessRuleException.class, () -> fixtureService.generateRoundRobinFixtures());

        teamService.registerTeam(new TeamRequest("Team A", "Capt A", "Dept A", "111"));
        assertThrows(BusinessRuleException.class, () -> fixtureService.generateRoundRobinFixtures());

        teamService.registerTeam(new TeamRequest("Team B", "Capt B", "Dept B", "222"));
        teamService.registerTeam(new TeamRequest("Team C", "Capt C", "Dept C", "333"));
        teamService.registerTeam(new TeamRequest("Team D", "Capt D", "Dept D", "444"));

        var fixtures = fixtureService.generateRoundRobinFixtures();
        assertEquals(3, fixtures.size()); // 4 teams = 3 rounds
        assertEquals(6, matchService.getAllMatches().size()); // 4*3/2 = 6 matches
    }

    @Test
    @DisplayName("Rule 4: Match result auto-updates points table following fixed scoring rule (Win=3, Loss=0)")
    void testMatchScoringUpdatesStandings() {
        Team t1 = teamService.registerTeam(new TeamRequest("Alpha", "A", "Dept A", "1"));
        Team t2 = teamService.registerTeam(new TeamRequest("Beta", "B", "Dept B", "2"));

        fixtureService.generateRoundRobinFixtures();
        List<Match> matches = matchService.getAllMatches();
        assertFalse(matches.isEmpty());

        Match match = matches.get(0);
        // Alpha wins 3 - 1 Beta
        Match recorded = matchService.recordResult(match.getId(), new MatchResultRequest(3, 1));
        assertEquals(MatchStatus.COMPLETED, recorded.getStatus());

        List<StandingsEntry> standings = standingsService.getStandings();
        StandingsEntry top = standings.get(0);
        StandingsEntry bottom = standings.get(1);

        assertEquals(match.getHomeTeam().getId(), top.getTeam().getId());
        assertEquals(1, top.getPlayed());
        assertEquals(1, top.getWon());
        assertEquals(3, top.getPoints());
        assertEquals(2, top.getGoalDifference());

        assertEquals(match.getAwayTeam().getId(), bottom.getTeam().getId());
        assertEquals(1, bottom.getPlayed());
        assertEquals(1, bottom.getLost());
        assertEquals(0, bottom.getPoints());
    }

    @Test
    @DisplayName("Rule 5: Draw result gives 1 point to both teams")
    void testDrawScoring() {
        teamService.registerTeam(new TeamRequest("Team 1", "A", "D1", "1"));
        teamService.registerTeam(new TeamRequest("Team 2", "B", "D2", "2"));

        fixtureService.generateRoundRobinFixtures();
        Match match = matchService.getAllMatches().get(0);

        matchService.recordResult(match.getId(), new MatchResultRequest(2, 2));

        List<StandingsEntry> standings = standingsService.getStandings();
        assertEquals(1, standings.get(0).getPoints());
        assertEquals(1, standings.get(1).getPoints());
        assertEquals(1, standings.get(0).getDrawn());
        assertEquals(1, standings.get(1).getDrawn());
    }

    @Test
    @DisplayName("Rule 6: A match result, once recorded, updates standings exactly once (no duplicate counting on edit without adjustment)")
    void testEditMatchResultAdjustsStandingsWithoutDuplication() {
        teamService.registerTeam(new TeamRequest("Team Home", "H", "DH", "1"));
        teamService.registerTeam(new TeamRequest("Team Away", "A", "DA", "2"));

        fixtureService.generateRoundRobinFixtures();
        Match match = matchService.getAllMatches().get(0);

        // First recording: Home wins 2 - 0 (Home: 3 pts, Away: 0 pts, Played: 1 each)
        matchService.recordResult(match.getId(), new MatchResultRequest(2, 0));

        List<StandingsEntry> standings1 = standingsService.getStandings();
        assertEquals(3, standings1.get(0).getPoints());
        assertEquals(1, standings1.get(0).getPlayed());

        // Edit recording: Change to 1 - 3 (Away now wins!)
        // Standings must adjust properly: Home: 0 pts, Away: 3 pts, Played: STILL 1 each!
        matchService.recordResult(match.getId(), new MatchResultRequest(1, 3));

        List<StandingsEntry> standings2 = standingsService.getStandings();
        StandingsEntry newTop = standings2.get(0);
        StandingsEntry newBottom = standings2.get(1);

        assertEquals(match.getAwayTeam().getId(), newTop.getTeam().getId());
        assertEquals(3, newTop.getPoints());
        assertEquals(1, newTop.getPlayed()); // NOT 2!
        assertEquals(1, newTop.getWon());

        assertEquals(match.getHomeTeam().getId(), newBottom.getTeam().getId());
        assertEquals(0, newBottom.getPoints());
        assertEquals(1, newBottom.getPlayed()); // NOT 2!
        assertEquals(1, newBottom.getLost());
    }

    @Test
    @DisplayName("Rule 7: Negative scores are rejected immediately with clear error message")
    void testNegativeScoresRejected() {
        teamService.registerTeam(new TeamRequest("T1", "A", "D1", "1"));
        teamService.registerTeam(new TeamRequest("T2", "B", "D2", "2"));

        fixtureService.generateRoundRobinFixtures();
        Match match = matchService.getAllMatches().get(0);

        assertThrows(BusinessRuleException.class, () -> {
            matchService.recordResult(match.getId(), new MatchResultRequest(-1, 2));
        });
    }
}
