# LeagueTrack

Intramural Sports Tournament Fixture and Standings system.

## Stack
- Spring Boot
- Spring Data JPA
- MySQL through XAMPP
- HTML/CSS/JavaScript
- IntelliJ IDEA
- Java 21

## Features
- Team registration
- Automatic round-robin fixture generation
- Match result recording
- Automatic points calculation
- Live standings table
- Tournament reset

## Points
Win = 3, Draw = 1, Loss = 0

## Run
1. Start MySQL in XAMPP.
2. Create database `league_track` in phpMyAdmin, or allow the JDBC URL to create it.
3. Check `application.properties` for MySQL username/password.
4. Open the project in IntelliJ IDEA.
5. Set Project SDK to Java 21.
6. Run `LeagueTrackApplication`.
7. Open http://localhost:8080
