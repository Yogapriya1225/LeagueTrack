package com.example.leaguetrack.repository;

import com.example.leaguetrack.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByRoundNumberAscIdAsc();
    void deleteAllByHomeTeam_IdOrAwayTeam_Id(Long homeTeamId, Long awayTeamId);
}
