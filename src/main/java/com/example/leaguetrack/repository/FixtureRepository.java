package com.example.leaguetrack.repository;

import com.example.leaguetrack.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {
    List<Fixture> findAllByOrderByRoundNumberAsc();
    Optional<Fixture> findByRoundNumber(Integer roundNumber);
}
