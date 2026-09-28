package com.example.leaguetrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "standings_entries")
public class StandingsEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "team_id", unique = true, nullable = false)
    private Team team;

    @Column(nullable = false)
    private int played = 0;

    @Column(nullable = false)
    private int won = 0;

    @Column(nullable = false)
    private int drawn = 0;

    @Column(nullable = false)
    private int lost = 0;

    @Column(nullable = false)
    private int goalsFor = 0;

    @Column(nullable = false)
    private int goalsAgainst = 0;

    @Column(nullable = false)
    private int goalDifference = 0;

    @Column(nullable = false)
    private int points = 0;

    private LocalDateTime lastUpdated;

    public StandingsEntry() {}

    public StandingsEntry(Team team) {
        this.team = team;
        this.lastUpdated = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.goalDifference = this.goalsFor - this.goalsAgainst;
        this.lastUpdated = LocalDateTime.now();
    }

    public void applyResult(int scored, int conceded, int winPts, int drawPts, int lossPts) {
        this.played++;
        this.goalsFor += scored;
        this.goalsAgainst += conceded;
        this.goalDifference = this.goalsFor - this.goalsAgainst;

        if (scored > conceded) {
            this.won++;
            this.points += winPts;
        } else if (scored < conceded) {
            this.lost++;
            this.points += lossPts;
        } else {
            this.drawn++;
            this.points += drawPts;
        }
        this.lastUpdated = LocalDateTime.now();
    }

    public void revertResult(int scored, int conceded, int winPts, int drawPts, int lossPts) {
        this.played = Math.max(0, this.played - 1);
        this.goalsFor = Math.max(0, this.goalsFor - scored);
        this.goalsAgainst = Math.max(0, this.goalsAgainst - conceded);
        this.goalDifference = this.goalsFor - this.goalsAgainst;

        if (scored > conceded) {
            this.won = Math.max(0, this.won - 1);
            this.points = Math.max(0, this.points - winPts);
        } else if (scored < conceded) {
            this.lost = Math.max(0, this.lost - 1);
            this.points = Math.max(0, this.points - lossPts);
        } else {
            this.drawn = Math.max(0, this.drawn - 1);
            this.points = Math.max(0, this.points - drawPts);
        }
        this.lastUpdated = LocalDateTime.now();
    }

    public void reset() {
        this.played = 0;
        this.won = 0;
        this.drawn = 0;
        this.lost = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.goalDifference = 0;
        this.points = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getWon() {
        return won;
    }

    public void setWon(int won) {
        this.won = won;
    }

    public int getDrawn() {
        return drawn;
    }

    public void setDrawn(int drawn) {
        this.drawn = drawn;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    public int getGoalDifference() {
        return goalDifference;
    }

    public void setGoalDifference(int goalDifference) {
        this.goalDifference = goalDifference;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
