package com.example.leaguetrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String captain;

    public Team() {}

    public Team(String name, String captain) {
        this.name = name;
        this.captain = captain;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCaptain() { return captain; }
    public void setCaptain(String captain) { this.captain = captain; }
}
