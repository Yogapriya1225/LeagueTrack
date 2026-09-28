package com.example.leaguetrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "leaguetrack.scoring")
public class ScoringConfig {

    private int winPoints = 3;
    private int drawPoints = 1;
    private int lossPoints = 0;

    public ScoringConfig() {}

    public ScoringConfig(int winPoints, int drawPoints, int lossPoints) {
        this.winPoints = winPoints;
        this.drawPoints = drawPoints;
        this.lossPoints = lossPoints;
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
}
