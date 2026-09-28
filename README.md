# LeagueTrack — Intramural Sports Tournament Fixture & Standings System

> **Sri Eshwar College of Engineering** — Project Leap (Java & DBMS Assessment)  
> **Course Code**: U23CS491 | **Course Title**: Java Programming  
> **Register Number**: 722825148122 | **Question**: 48. LeagueTrack  

---

## 📌 Problem Scenario
Intramural sports committees schedule matches and track standings on whiteboards, making it hard to keep everyone updated on fixtures and points. **LeagueTrack** automates round-robin fixture generation, enforces strict business rules for match scoring, auto-calculates tournament standings, and provides an attractive, responsive multi-view web interface.

---

## 🛠️ Technology Stack
- **Backend**: Spring Boot 3.5.6, Java 21, Spring Data JPA, Jakarta Bean Validation, Springdoc OpenAPI 3 (Swagger)
- **Database**: MySQL 8.x via XAMPP (Port 3306, database `league_track`)
- **Server Port**: `8081` (`http://localhost:8081`)
- **Frontend**: HTML5, Vanilla CSS3 (Athletic Glassmorphism Design System), JavaScript (ES6+ Fetch API)
- **Build Tool**: Apache Maven 3.9+

---

## 🗄️ Database Architecture & Entities

LeagueTrack implements a layered relational architecture with 4 primary JPA entities:

1. **`Team`** (`teams` table):
   - `id` (PK, Auto-Increment)
   - `name` (Unique, Not Null, e.g. "CSE Titans")
   - `captain` (Captain name)
   - `department` (Branch/Department, e.g. "CSE", "ECE", "MECH")
   - `contactNumber` (Contact phone)
   - `createdAt` (Audit timestamp)

2. **`Fixture`** (`fixtures` table):
   - `id` (PK, Auto-Increment)
   - `roundNumber` (Integer, round index)
   - `roundName` (String, e.g. "Round 1")
   - `description` (Description)
   - `matches` (`@OneToMany` relationship with cascade and orphan removal)

3. **`Match`** (`matches` table):
   - `id` (PK, Auto-Increment)
   - `fixture_id` (`@ManyToOne` referencing `Fixture`)
   - `home_team_id` (`@ManyToOne` referencing `Team`)
   - `away_team_id` (`@ManyToOne` referencing `Team`)
   - `roundNumber` (Integer)
   - `homeScore` (Integer, nullable until played)
   - `awayScore` (Integer, nullable until played)
   - `status` (`SCHEDULED`, `COMPLETED`)
   - `venue` (e.g. "Main Sports Arena", "Court A")
   - `completedAt` (Timestamp)

4. **`StandingsEntry`** (`standings_entries` table):
   - `id` (PK, Auto-Increment)
   - `team_id` (`@OneToOne` referencing `Team`, unique)
   - `played`, `won`, `drawn`, `lost` (Match outcome counts)
   - `goalsFor`, `goalsAgainst`, `goalDifference`
   - `points` (Calculated according to configurable rules)
   - `lastUpdated` (Timestamp)

---

## ⚖️ Business Rules Enforced

1. **Configurable Scoring Rules**:
   - Win, Draw, and Loss points are configurable via `application.properties` (`leaguetrack.scoring.win-points=3`, `draw-points=1`, `loss-points=0`) and dynamic REST API endpoints (`/api/standings/rules`).
2. **Strict Standings Idempotency & Edit Rollback**:
   - A match result updates standings exactly once.
   - When a match result is edited or updated, previous scores are automatically rolled back from the standings table before applying new scores, preventing duplicate counting.
3. **Input Validation**:
   - Team names must be non-blank and unique (case-insensitive).
   - Match scores cannot be negative or null.
   - All violations return structured JSON errors with HTTP 400 Bad Request.
4. **Automated Round-Robin Scheduling**:
   - Circle/polygon rotation algorithm pairs every team against every other team once.
   - Automatically handles odd numbers of teams with a BYE mechanism.
5. **Real-Time Notification & Audit Logging**:
   - All state mutations (registration, fixture generation, score updates, resets) log structured audit events to console and in-memory activity feeds.

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/teams` | List all registered teams |
| `GET` | `/api/teams/page?page=0&size=10` | Paginated and sorted teams |
| `POST` | `/api/teams` | Register a new team (auto-creates standings entry) |
| `PUT` | `/api/teams/{id}` | Update team information |
| `DELETE` | `/api/teams/{id}` | Delete a team and its related matches |
| `POST` | `/api/fixtures/generate` | Auto-generate round-robin fixtures |
| `GET` | `/api/fixtures` | Get all rounds and fixtures |
| `GET` | `/api/matches` | Get all scheduled and completed matches |
| `PUT` | `/api/matches/{id}/result` | Record or edit match result (auto-updates standings) |
| `POST` | `/api/matches/reset-matches` | Clear fixtures/results, reset points to 0, keep teams |
| `DELETE` | `/api/matches/reset` | Complete league reset (teams, matches, standings) |
| `GET` | `/api/standings` | View official points table sorted by points, GD, GF |
| `GET` | `/api/standings/rules` | View current win/draw/loss scoring rules |
| `PUT` | `/api/standings/rules` | Update scoring rules dynamically & recalculate table |
| `POST` | `/api/standings/recalculate` | Re-run full standings recalculation from matches |
| `GET` | `/api/activities` | Live event and tournament audit stream |

---

## 🚀 How to Run the Application

### Step 1: Start MySQL in XAMPP
1. Open the **XAMPP Control Panel**.
2. Click **Start** on the **MySQL** module (default port `3306`).
3. Ensure MySQL is running. (Database `league_track` will be auto-created if it does not already exist).

### Step 2: Run the Spring Boot Backend
In the terminal, run:
```bash
mvn spring-boot:run
```
*(or run `LeagueTrackApplication.java` directly in IntelliJ IDEA or your IDE)*.

### Step 3: Open the Web Application
Open your browser and navigate to:
```
http://localhost:8081
```

### Step 4: Access Swagger API Documentation
```
http://localhost:8081/swagger-ui/index.html
```
