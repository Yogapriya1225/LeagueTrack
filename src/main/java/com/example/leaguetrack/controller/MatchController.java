package com.example.leaguetrack.controller;

import com.example.leaguetrack.dto.MatchResultRequest;
import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<Match>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Match>> getMatchesPaged(
            @PageableDefault(size = 10, sort = "roundNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(matchService.getAllMatches(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Match>> generateFixtures() {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.generateFixtures());
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<Match> recordResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchResultRequest resultRequest) {
        Match updated = matchService.recordResult(id, resultRequest);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/standings")
    public ResponseEntity<List<StandingsEntry>> getStandings() {
        return ResponseEntity.ok(matchService.getStandings());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetTournament() {
        matchService.resetTournament();
        return ResponseEntity.ok(Map.of("message", "Tournament data has been completely reset"));
    }

    @PostMapping("/reset-matches")
    public ResponseEntity<Map<String, String>> resetMatchesOnly() {
        matchService.resetMatchesOnly();
        return ResponseEntity.ok(Map.of("message", "Matches cleared and standings reset. Registered teams preserved."));
    }
}
