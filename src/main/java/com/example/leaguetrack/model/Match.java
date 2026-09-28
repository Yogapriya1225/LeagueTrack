package com.example.leaguetrack.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fixture_id")
    @JsonBackReference
    private Fixture fixture;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(nullable = false)
    private Integer roundNumber;

    private Integer homeScore;
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(length = 100)
    private String venue;

    private LocalDateTime matchDate;

    private LocalDateTime completedAt;

    public Match() {}

    public Match(Team homeTeam, Team awayTeam, Integer roundNumber) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.roundNumber = roundNumber;
        this.status = MatchStatus.SCHEDULED;
    }

    public Match(Fixture fixture, Team homeTeam, Team awayTeam, Integer roundNumber, String venue) {
        this.fixture = fixture;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.roundNumber = roundNumber;
        this.status = MatchStatus.SCHEDULED;
        this.venue = venue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public void setFixture(Fixture fixture) {
        this.fixture = fixture;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
