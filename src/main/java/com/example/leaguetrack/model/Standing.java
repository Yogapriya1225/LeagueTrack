package com.example.leaguetrack.model;

public class Standing {
    private Long teamId;
    private String teamName;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int goalDifference;
    private int points;

    public Standing() {}

    public Standing(Long teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public Standing(Long teamId, String teamName, int played, int wins, int draws, int losses, int goalsFor, int goalsAgainst, int goalDifference, int points) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.played = played;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalDifference = goalDifference;
        this.points = points;
    }

    public static Standing fromEntity(StandingsEntry entry) {
        return new Standing(
                entry.getTeam().getId(),
                entry.getTeam().getName(),
                entry.getPlayed(),
                entry.getWon(),
                entry.getDrawn(),
                entry.getLost(),
                entry.getGoalsFor(),
                entry.getGoalsAgainst(),
                entry.getGoalDifference(),
                entry.getPoints()
        );
    }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getGoalsFor() { return goalsFor; }
    public void setGoalsFor(int goalsFor) { this.goalsFor = goalsFor; }

    public int getGoalsAgainst() { return goalsAgainst; }
    public void setGoalsAgainst(int goalsAgainst) { this.goalsAgainst = goalsAgainst; }

    public int getGoalDifference() { return goalDifference; }
    public void setGoalDifference(int goalDifference) { this.goalDifference = goalDifference; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public void recordWin(int winPoints) { played++; wins++; points += winPts(winPoints); }
    public void recordDraw(int drawPoints) { played++; draws++; points += drawPts(drawPoints); }
    public void recordLoss(int lossPoints) { played++; losses++; points += lossPoints; }

    private int winPts(int p) { return p; }
    private int drawPts(int p) { return p; }
}
