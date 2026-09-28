package com.example.leaguetrack.repository;

import com.example.leaguetrack.model.StandingsEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StandingsEntryRepository extends JpaRepository<StandingsEntry, Long> {
    Optional<StandingsEntry> findByTeam_Id(Long teamId);
    List<StandingsEntry> findAllByOrderByPointsDescGoalDifferenceDescGoalsForDescWonDescTeam_NameAsc();
    void deleteByTeam_Id(Long teamId);
}
