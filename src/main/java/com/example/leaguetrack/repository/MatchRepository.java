package com.example.leaguetrack.repository;

import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByRoundNumberAscIdAsc();
    List<Match> findByFixtureId(Long fixtureId);
    List<Match> findByStatus(MatchStatus status);
    List<Match> findByHomeTeam_IdOrAwayTeam_Id(Long homeTeamId, Long awayTeamId);
    void deleteAllByHomeTeam_IdOrAwayTeam_Id(Long homeTeamId, Long awayTeamId);
}
