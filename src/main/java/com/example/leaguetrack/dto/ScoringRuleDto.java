package com.example.leaguetrack.dto;

public class ScoringRuleDto {
    private int winPoints;
    private int drawPoints;
    private int lossPoints;
    private String description;

    public ScoringRuleDto() {}

    public ScoringRuleDto(int winPoints, int drawPoints, int lossPoints, String description) {
        this.winPoints = winPoints;
        this.drawPoints = drawPoints;
        this.lossPoints = lossPoints;
        this.description = description;
    }

    public int getWinPoints() {
        return winPoints;
    }

    public void setWinPoints(int winPoints) {
        this.winPoints = winPoints;
    }

    public int getDrawPoints() {
        return drawPoints;
    }

    public void setDrawPoints(int drawPoints) {
        this.drawPoints = drawPoints;
    }

    public int getLossPoints() {
        return lossPoints;
    }

    public void setLossPoints(int lossPoints) {
        this.lossPoints = lossPoints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
