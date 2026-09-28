package com.example.leaguetrack.controller;

import com.example.leaguetrack.model.Fixture;
import com.example.leaguetrack.service.FixtureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
@CrossOrigin(origins = "*")
public class FixtureController {

    private final FixtureService fixtureService;

    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    @GetMapping
    public ResponseEntity<List<Fixture>> getAllFixtures() {
        return ResponseEntity.ok(fixtureService.getAllFixtures());
    }

    @GetMapping("/round/{roundNumber}")
    public ResponseEntity<Fixture> getFixtureByRound(@PathVariable Integer roundNumber) {
        return ResponseEntity.ok(fixtureService.getFixtureByRound(roundNumber));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Fixture>> generateFixtures() {
        List<Fixture> fixtures = fixtureService.generateRoundRobinFixtures();
        return ResponseEntity.status(HttpStatus.CREATED).body(fixtures);
    }
}
