package com.example.leaguetrack.model;

public class Standing {
    private Long teamId;
    private String teamName;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int points;

    public Standing(Long teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public Long getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getPoints() { return points; }

    public void recordWin() { played++; wins++; points += 3; }
    public void recordDraw() { played++; draws++; points += 1; }
    public void recordLoss() { played++; losses++; }
}
