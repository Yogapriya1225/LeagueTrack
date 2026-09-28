package com.example.leaguetrack.controller;

import com.example.leaguetrack.model.Match;
import com.example.leaguetrack.model.Standing;
import com.example.leaguetrack.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<Match> getAllMatches() {
        return matchService.getAllMatches();
    }

    @PostMapping("/generate")
    public List<Match> generateFixtures() {
        return matchService.generateFixtures();
    }

    @PutMapping("/{id}/result")
    public Match recordResult(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> result) {
        return matchService.recordResult(
                id,
                result.get("homeScore"),
                result.get("awayScore")
        );
    }

    @GetMapping("/standings")
    public List<Standing> getStandings() {
        return matchService.getStandings();
    }

    @DeleteMapping("/reset")
    public void resetTournament() {
        matchService.resetTournament();
    }
}
