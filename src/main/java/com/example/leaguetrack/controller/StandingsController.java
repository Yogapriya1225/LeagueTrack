package com.example.leaguetrack.controller;

import com.example.leaguetrack.dto.ScoringRuleDto;
import com.example.leaguetrack.model.StandingsEntry;
import com.example.leaguetrack.service.StandingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/standings")
@CrossOrigin(origins = "*")
public class StandingsController {

    private final StandingsService standingsService;

    public StandingsController(StandingsService standingsService) {
        this.standingsService = standingsService;
    }

    @GetMapping
    public ResponseEntity<List<StandingsEntry>> getStandings() {
        return ResponseEntity.ok(standingsService.getStandings());
    }

    @GetMapping("/rules")
    public ResponseEntity<ScoringRuleDto> getScoringRules() {
        return ResponseEntity.ok(standingsService.getScoringRule());
    }

    @PutMapping("/rules")
    public ResponseEntity<ScoringRuleDto> updateScoringRules(@RequestBody Map<String, Integer> rules) {
        int win = rules.getOrDefault("winPoints", 3);
        int draw = rules.getOrDefault("drawPoints", 1);
        int loss = rules.getOrDefault("lossPoints", 0);
        return ResponseEntity.ok(standingsService.updateScoringRule(win, draw, loss));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<List<StandingsEntry>> recalculateStandings() {
        return ResponseEntity.ok(standingsService.recalculateStandings());
    }
}
