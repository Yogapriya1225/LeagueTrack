/**
 * LeagueTrack - Professional Sports Dashboard & Match Engine
 * Frontend Controller & API Integration
 */

// Global Application State
const state = {
    teams: [],
    matches: [],
    fixtures: [],
    standings: [],
    scoringRule: { winPoints: 3, drawPoints: 1, lossPoints: 0, description: "Win: 3 pts | Draw: 1 pt | Loss: 0 pts" },
    activeRoundFilter: "ALL",
    activeStatusFilter: "ALL"
};

/* ==========================================================================
   API Client
   ========================================================================== */

async function api(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                "Content-Type": "application/json",
                ...(options.headers || {})
            },
            ...options
        });

        if (response.status === 204) return null;

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            let errorMsg = "An error occurred";
            if (data?.validationErrors) {
                errorMsg = Object.values(data.validationErrors).join(" | ");
            } else if (data?.message) {
                errorMsg = data.message;
            } else if (data?.error) {
                errorMsg = data.error;
            }
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        throw err;
    }
}

/* ==========================================================================
   Toast Notifications
   ========================================================================== */

function showToast(message, type = "info") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    let icon = "ℹ️";
    if (type === "success") icon = "✅";
    if (type === "error") icon = "⚠️";

    toast.innerHTML = `<span>${icon}</span><span style="flex:1;">${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(50px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

/* ==========================================================================
   Tab Navigation & Breadcrumb Updater
   ========================================================================== */

const VIEW_NAMES = {
    tabDashboard: "Live Dashboard",
    tabTeams: "Teams & Roster",
    tabFixtures: "Fixtures & Matches",
    tabStandings: "Standings Table",
    tabSettings: "Scoring & Rules"
};

function switchTab(tabId) {
    document.querySelectorAll(".nav-item").forEach(item => {
        if (item.getAttribute("data-tab") === tabId) {
            item.classList.add("active");
        } else {
            item.classList.remove("active");
        }
    });

    document.querySelectorAll(".tab-panel").forEach(panel => {
        if (panel.id === tabId) {
            panel.classList.add("active");
        } else {
            panel.classList.remove("active");
        }
    });

    const viewLabel = VIEW_NAMES[tabId] || "Live Center";
    const breadcrumbElem = document.getElementById("currentViewName");
    if (breadcrumbElem) breadcrumbElem.textContent = viewLabel;

    window.scrollTo({ top: 0, behavior: "smooth" });
}

/* ==========================================================================
   Hero Showcase Banner & Dashboard KPIs
   ========================================================================== */

function updateDashboardShowcase() {
    const totalTeams = state.teams.length;
    const completedMatches = state.matches.filter(m => m.status === "COMPLETED");
    const totalMatches = state.matches.length;
    const totalGoals = state.matches.reduce((acc, m) => acc + (m.homeScore || 0) + (m.awayScore || 0), 0);

    // Update Mini Stat Boxes
    document.getElementById("statTotalTeams").textContent = totalTeams;
    document.getElementById("statMatchRatio").textContent = `${completedMatches.length} / ${totalMatches}`;
    document.getElementById("statTotalGoals").textContent = `${totalGoals} Pts`;
    document.getElementById("statScoringPill").textContent = `${state.scoringRule.winPoints} / ${state.scoringRule.drawPoints} / ${state.scoringRule.lossPoints} PTS`;

    // Sidebar Pills
    document.getElementById("sideTeamCount").textContent = totalTeams;
    document.getElementById("sideMatchCount").textContent = totalMatches;

    // Top Leader Showcase (Like FC Barcelona in Screenshot!)
    const heroName = document.getElementById("heroLeaderName");
    const heroSubtitle = document.getElementById("heroSubtitle");
    const heroCrest = document.getElementById("heroLeaderCrest");
    const statLeaderPts = document.getElementById("statLeaderPoints");

    if (state.standings && state.standings.length > 0) {
        const leader = state.standings[0];
        heroName.textContent = leader.team.name;
        heroSubtitle.textContent = `${leader.team.department || "CHAMPIONSHIP DIVISION"} &bull; 1ST PLACE`;
        heroCrest.textContent = leader.team.name.charAt(0).toUpperCase();
        statLeaderPts.textContent = `${leader.points} Pts (${leader.won}W)`;

        document.getElementById("chartTop1").textContent = `${leader.team.name} (${leader.points} pts)`;
    } else {
        heroName.textContent = "Tournament Kickoff";
        heroSubtitle.textContent = "LEADERBOARD TOP TEAM";
        heroCrest.textContent = "🏆";
        statLeaderPts.textContent = "-";
        document.getElementById("chartTop1").textContent = "-";
    }

    document.getElementById("chartGoals").textContent = totalGoals;
    document.getElementById("chartMatchesCount").textContent = `${completedMatches.length} of ${totalMatches}`;

    renderShortenedTable();
    renderMatchWidgets();
    renderDashboardTeamsTable();
    updateTrendChartVisuals();
}

/* ==========================================================================
   Shortened League Table (Screenshot Widget)
   ========================================================================== */

function renderShortenedTable() {
    const tbody = document.getElementById("shortenedTableBody");
    if (!tbody) return;

    if (!state.standings || state.standings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No standings yet.</td></tr>`;
        return;
    }

    // Top 5 teams rendered in shortened table
    tbody.innerHTML = state.standings.slice(0, 5).map((entry, idx) => {
        const isLeader = idx === 0;
        const gd = entry.goalDifference > 0 ? `+${entry.goalDifference}` : entry.goalDifference;

        return `
            <tr class="${isLeader ? 'leader-row' : ''}">
                <td>
                    <span class="pos-pill ${idx === 0 ? 'pos-1' : (idx === 1 ? 'pos-2' : (idx === 2 ? 'pos-3' : 'pos-def'))}">
                        ${idx + 1}
                    </span>
                </td>
                <td>
                    <strong style="color: ${isLeader ? '#fff' : '#e2e8f0'};">${escapeHtml(entry.team.name)}</strong>
                </td>
                <td class="text-center">${entry.played}</td>
                <td class="text-center">${gd}</td>
                <td class="text-center" style="font-weight: 800; font-family: var(--font-heading);">${entry.points}</td>
            </tr>
        `;
    }).join("");
}

/* ==========================================================================
   Previous & Next Match Widgets (From Screenshot)
   ========================================================================== */

function renderMatchWidgets() {
    const prevWidget = document.getElementById("prevMatchWidget");
    const nextWidget = document.getElementById("nextMatchWidget");

    // Previous match (most recently completed match)
    const completedMatches = state.matches.filter(m => m.status === "COMPLETED");
    if (completedMatches.length > 0 && prevWidget) {
        const last = completedMatches[completedMatches.length - 1];
        const homeWon = last.homeScore > last.awayScore;
        const awayWon = last.awayScore > last.homeScore;

        prevWidget.innerHTML = `
            <div class="widget-teams-row">
                <div class="widget-team-col">
                    <div class="widget-team-crest">${last.homeTeam.name.charAt(0)}</div>
                    <span class="widget-team-title" title="${escapeHtml(last.homeTeam.name)}">${escapeHtml(last.homeTeam.name)}</span>
                </div>
                <div class="widget-score-pill">${last.homeScore} - ${last.awayScore}</div>
                <div class="widget-team-col">
                    <div class="widget-team-crest">${last.awayTeam.name.charAt(0)}</div>
                    <span class="widget-team-title" title="${escapeHtml(last.awayTeam.name)}">${escapeHtml(last.awayTeam.name)}</span>
                </div>
            </div>
            <div style="display:flex; justify-content:center;">
                <span class="widget-badge-win">${homeWon ? last.homeTeam.name + ' Won' : (awayWon ? last.awayTeam.name + ' Won' : 'Draw Match')}</span>
            </div>
            <span class="text-muted text-xs text-center" style="margin-top:2px;">Round ${last.roundNumber} &bull; ${escapeHtml(last.venue || "Arena")}</span>
        `;
    } else if (prevWidget) {
        prevWidget.innerHTML = `<div class="text-muted text-sm text-center py-4">No completed match yet.</div>`;
    }

    // Next match (first scheduled match)
    const scheduledMatches = state.matches.filter(m => m.status === "SCHEDULED");
    if (scheduledMatches.length > 0 && nextWidget) {
        const next = scheduledMatches[0];
        nextWidget.innerHTML = `
            <div class="widget-teams-row">
                <div class="widget-team-col">
                    <div class="widget-team-crest" style="background: linear-gradient(135deg, #0284c7, #0369a1);">${next.homeTeam.name.charAt(0)}</div>
                    <span class="widget-team-title" title="${escapeHtml(next.homeTeam.name)}">${escapeHtml(next.homeTeam.name)}</span>
                </div>
                <span style="font-weight:900; font-family:var(--font-heading); color:var(--text-muted); font-size:14px;">VS</span>
                <div class="widget-team-col">
                    <div class="widget-team-crest" style="background: linear-gradient(135deg, #d97706, #b45309);">${next.awayTeam.name.charAt(0)}</div>
                    <span class="widget-team-title" title="${escapeHtml(next.awayTeam.name)}">${escapeHtml(next.awayTeam.name)}</span>
                </div>
            </div>
            <div style="display:flex; justify-content:center; margin-top:4px;">
                <button class="btn btn-sm btn-flame" style="padding: 4px 12px; font-size: 11px;" onclick="openScoreModal(${next.id}, false)">
                    Record Result
                </button>
            </div>
            <span class="text-muted text-xs text-center" style="margin-top:2px;">Round ${next.roundNumber} &bull; ${escapeHtml(next.venue || "Campus Court")}</span>
        `;
    } else if (nextWidget) {
        nextWidget.innerHTML = `<div class="text-muted text-sm text-center py-4">All fixtures played!</div>`;
    }
}

/* ==========================================================================
   Dashboard Lower Teams Roster Table
   ========================================================================== */

function renderDashboardTeamsTable() {
    const tbody = document.getElementById("dashTeamsTableBody");
    if (!tbody) return;

    if (!state.teams || state.teams.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No teams registered in the league roster yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.teams.map((t, idx) => `
        <tr>
            <td style="font-family: var(--font-mono); color: var(--text-muted);">#${t.id}</td>
            <td><strong class="cell-team-name">${escapeHtml(t.name)}</strong></td>
            <td>${escapeHtml(t.captain || "-")}</td>
            <td><span class="badge badge-cyan">${escapeHtml(t.department || "General")}</span></td>
            <td>${escapeHtml(t.contactNumber || "-")}</td>
            <td class="text-right">
                <button class="btn btn-sm btn-ghost" onclick="openEditTeamModal(${t.id})">Edit</button>
                <button class="btn btn-sm btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="deleteTeam(${t.id}, '${escapeHtml(t.name)}')">Delete</button>
            </td>
        </tr>
    `).join("");
}

/* ==========================================================================
   Dynamic SVG Trend Chart Animation
   ========================================================================== */

function updateTrendChartVisuals() {
    const totalRounds = [...new Set(state.matches.map(m => m.roundNumber))].length || 5;
    const completedMatches = state.matches.filter(m => m.status === "COMPLETED");

    const svgArea = document.getElementById("svgAreaPath");
    const svgLine = document.getElementById("svgLinePath");
    const nodesGroup = document.getElementById("svgNodesGroup");

    if (!svgArea || !svgLine || !nodesGroup) return;

    if (completedMatches.length === 0) {
        // Base flat curve
        svgArea.setAttribute("d", "M 60,190 L 180,190 L 300,190 L 420,190 L 540,190 L 660,190 L 760,190 L 760,200 L 60,200 Z");
        svgLine.setAttribute("d", "M 60,190 L 180,190 L 300,190 L 420,190 L 540,190 L 660,190 L 760,190");
        return;
    }

    // Dynamic upward curve based on completed matches and top scores
    const yTop = Math.max(35, 190 - (completedMatches.length * 20));
    const pathD = `M 60,185 L 180,165 L 300,135 L 420,${Math.max(60, yTop + 30)} L 540,${Math.max(45, yTop + 15)} L 660,${Math.max(38, yTop + 5)} L 760,${yTop}`;
    const areaD = `${pathD} L 760,200 L 60,200 Z`;

    svgArea.setAttribute("d", areaD);
    svgLine.setAttribute("d", pathD);
}

/* ==========================================================================
   Teams Management View
   ========================================================================== */

async function loadTeams() {
    try {
        const teams = await api("/api/teams");
        state.teams = teams || [];
        renderTeamsCards(state.teams);
        updateDashboardShowcase();
    } catch (error) {
        showToast("Failed to load teams: " + error.message, "error");
    }
}

function renderTeamsCards(teamsToRender) {
    const container = document.getElementById("teamsListContainer");
    if (!container) return;

    if (!teamsToRender || teamsToRender.length === 0) {
        container.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: var(--text-muted);">
                <p style="font-size: 28px; margin-bottom: 8px;">🛡️</p>
                <p>No registered teams yet. Enroll teams using the form on the left.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = teamsToRender.map(team => {
        const initial = team.name.charAt(0).toUpperCase();
        return `
            <div class="team-card-pro">
                <div>
                    <div class="team-card-head">
                        <div class="team-crest-badge">${initial}</div>
                        <div style="overflow: hidden;">
                            <h4 class="team-name-title" title="${escapeHtml(team.name)}">${escapeHtml(team.name)}</h4>
                            <span class="team-dept-badge">${escapeHtml(team.department || "General Branch")}</span>
                        </div>
                    </div>
                    <div class="team-meta-list">
                        <div><strong>Captain:</strong> ${escapeHtml(team.captain || "Not Assigned")}</div>
                        <div><strong>Contact:</strong> ${escapeHtml(team.contactNumber || "-")}</div>
                    </div>
                </div>
                <div class="team-card-foot">
                    <button class="btn btn-sm btn-outline" style="flex:1;" onclick="openEditTeamModal(${team.id})">Edit</button>
                    <button class="btn btn-sm btn-danger" style="padding: 4px 10px;" onclick="deleteTeam(${team.id}, '${escapeHtml(team.name)}')">Delete</button>
                </div>
            </div>
        `;
    }).join("");
}

function filterTeamsList() {
    const query = document.getElementById("teamSearchInput")?.value.toLowerCase().trim() || "";
    const filtered = state.teams.filter(t =>
        t.name.toLowerCase().includes(query) ||
        (t.captain && t.captain.toLowerCase().includes(query)) ||
        (t.department && t.department.toLowerCase().includes(query))
    );
    renderTeamsCards(filtered);
}

async function handleTeamSubmit(event) {
    event.preventDefault();
    const btn = document.getElementById("btnSubmitTeam");
    const originalText = btn.innerHTML;

    const payload = {
        name: document.getElementById("teamNameInput").value.trim(),
        captain: document.getElementById("captainInput").value.trim(),
        department: document.getElementById("departmentInput").value.trim(),
        contactNumber: document.getElementById("contactInput").value.trim()
    };

    try {
        btn.disabled = true;
        btn.innerHTML = "Registering...";

        await api("/api/teams", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        showToast(`Team '${payload.name}' registered & standings initialized!`, "success");
        document.getElementById("teamRegisterForm").reset();

        await loadTeams();
        await loadStandings();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

function openEditTeamModal(teamId) {
    const team = state.teams.find(t => t.id === teamId);
    if (!team) return;

    document.getElementById("editTeamId").value = team.id;
    document.getElementById("editTeamName").value = team.name;
    document.getElementById("editCaptain").value = team.captain || "";
    document.getElementById("editDepartment").value = team.department || "";
    document.getElementById("editContact").value = team.contactNumber || "";

    document.getElementById("editTeamModal").style.display = "flex";
}

function closeEditTeamModal() {
    document.getElementById("editTeamModal").style.display = "none";
}

async function submitEditTeam(event) {
    event.preventDefault();
    const id = document.getElementById("editTeamId").value;
    const payload = {
        name: document.getElementById("editTeamName").value.trim(),
        captain: document.getElementById("editCaptain").value.trim(),
        department: document.getElementById("editDepartment").value.trim(),
        contactNumber: document.getElementById("editContact").value.trim()
    };

    try {
        await api(`/api/teams/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        });

        showToast("Team roster updated successfully", "success");
        closeEditTeamModal();
        await loadTeams();
        await loadStandings();
        await loadMatches();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function deleteTeam(id, name) {
    if (!confirm(`Delete team '${name}'?\nThis removes all linked fixtures and standings for this team.`)) {
        return;
    }

    try {
        await api(`/api/teams/${id}`, { method: "DELETE" });
        showToast(`Team '${name}' deleted`, "info");
        await loadTeams();
        await loadMatches();
        await loadStandings();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    }
}

/* ==========================================================================
   Fixtures & Match Center View
   ========================================================================== */

async function generateFixtures() {
    if (state.teams.length < 2) {
        showToast("Register at least 2 teams before generating fixtures", "error");
        switchTab("tabTeams");
        return;
    }

    const confirmMsg = state.matches.length > 0
        ? "Generating fixtures will overwrite existing matches and reset points. Continue?"
        : `Generate round-robin tournament for ${state.teams.length} teams?`;

    if (!confirm(confirmMsg)) return;

    try {
        const fixtures = await api("/api/fixtures/generate", { method: "POST" });
        showToast(`Generated ${fixtures.length} rounds of fixtures!`, "success");

        await loadMatches();
        await loadStandings();
        await loadActivities();
        switchTab("tabFixtures");
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function loadMatches() {
    try {
        const matches = await api("/api/matches");
        state.matches = matches || [];

        buildRoundFilterPills();
        renderFixtures();
        updateDashboardShowcase();
    } catch (error) {
        showToast("Failed to load match fixtures: " + error.message, "error");
    }
}

function buildRoundFilterPills() {
    const container = document.getElementById("roundFilterPills");
    if (!container) return;

    const rounds = [...new Set(state.matches.map(m => m.roundNumber))].sort((a, b) => a - b);

    let html = `<button class="filter-pill ${state.activeRoundFilter === 'ALL' ? 'active' : ''}" onclick="filterFixturesByRound('ALL')">All Rounds</button>`;
    rounds.forEach(r => {
        html += `<button class="filter-pill ${state.activeRoundFilter === String(r) ? 'active' : ''}" onclick="filterFixturesByRound('${r}')">Round ${r}</button>`;
    });

    container.innerHTML = html;
}

function filterFixturesByRound(round) {
    state.activeRoundFilter = round;
    buildRoundFilterPills();
    renderFixtures();
}

function filterFixturesByStatus(status) {
    state.activeStatusFilter = status;
    renderFixtures();
}

function renderFixtures() {
    const container = document.getElementById("fixturesListContainer");
    if (!container) return;

    if (!state.matches || state.matches.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 48px; color: var(--text-muted);">
                <p style="font-size: 32px; margin-bottom: 12px;">⚽</p>
                <h3 style="color: #fff; margin-bottom: 8px;">No Fixtures Scheduled</h3>
                <p style="margin-bottom: 18px;">Click <strong>Auto-Generate Schedule</strong> to generate fair round-robin match fixtures.</p>
                <button class="btn btn-flame" onclick="generateFixtures()">⚡ Auto-Generate Schedule</button>
            </div>
        `;
        return;
    }

    let filtered = state.matches;
    if (state.activeRoundFilter !== "ALL") {
        filtered = filtered.filter(m => String(m.roundNumber) === state.activeRoundFilter);
    }
    if (state.activeStatusFilter !== "ALL") {
        filtered = filtered.filter(m => m.status === state.activeStatusFilter);
    }

    if (filtered.length === 0) {
        container.innerHTML = `<div class="text-center text-muted py-5">No fixtures match current filter.</div>`;
        return;
    }

    const grouped = {};
    filtered.forEach(m => {
        if (!grouped[m.roundNumber]) grouped[m.roundNumber] = [];
        grouped[m.roundNumber].push(m);
    });

    let html = "";
    Object.keys(grouped).sort((a, b) => Number(a) - Number(b)).forEach(roundNum => {
        const matchesInRound = grouped[roundNum];
        const completed = matchesInRound.filter(m => m.status === "COMPLETED").length;

        html += `
            <div class="round-block">
                <div class="round-block-header">
                    <div class="round-block-title">
                        <span>🗓️</span> Round ${roundNum} Schedule
                    </div>
                    <span class="badge ${completed === matchesInRound.length ? 'badge-emerald' : 'badge-flame'}">
                        ${completed} / ${matchesInRound.length} Completed
                    </span>
                </div>
                <div class="fixtures-cards-grid">
                    ${matchesInRound.map(m => renderFixtureCard(m)).join("")}
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function renderFixtureCard(match) {
    const isCompleted = match.status === "COMPLETED";

    return `
        <div class="match-fixture-card ${isCompleted ? 'is-completed' : 'is-scheduled'}">
            <div class="fixture-top-bar">
                <span>📍 ${escapeHtml(match.venue || "Campus Turf Ground")}</span>
                <span class="badge ${isCompleted ? 'badge-emerald' : 'badge-gold'}">
                    ${isCompleted ? 'COMPLETED' : 'SCHEDULED'}
                </span>
            </div>

            <div class="fixture-versus-row">
                <div class="fixture-team-block">
                    <span class="fixture-team-name" title="${escapeHtml(match.homeTeam.name)}">${escapeHtml(match.homeTeam.name)}</span>
                    <span class="fixture-team-sub">${escapeHtml(match.homeTeam.department || "Home")}</span>
                </div>

                <div class="fixture-score-center">
                    ${isCompleted ? `
                        <div class="score-box-display">${match.homeScore} - ${match.awayScore}</div>
                    ` : `
                        <div class="score-box-display pending">VS</div>
                    `}
                </div>

                <div class="fixture-team-block away">
                    <span class="fixture-team-name" title="${escapeHtml(match.awayTeam.name)}">${escapeHtml(match.awayTeam.name)}</span>
                    <span class="fixture-team-sub">${escapeHtml(match.awayTeam.department || "Away")}</span>
                </div>
            </div>

            <div class="fixture-card-footer">
                ${isCompleted ? `
                    <button class="btn btn-sm btn-outline" onclick="openScoreModal(${match.id}, true)">
                        ✏️ Edit Score
                    </button>
                ` : `
                    <button class="btn btn-sm btn-flame" onclick="openScoreModal(${match.id}, false)">
                        Record Result
                    </button>
                `}
            </div>
        </div>
    `;
}

function openScoreModal(matchId, isEdit) {
    const match = state.matches.find(m => m.id === matchId);
    if (!match) return;

    document.getElementById("modalMatchId").value = match.id;
    document.getElementById("modalRoundBadge").textContent = `Round ${match.roundNumber}`;
    document.getElementById("modalTitle").textContent = isEdit ? "Edit Match Result" : "Record Match Result";
    document.getElementById("modalHomeName").textContent = match.homeTeam.name;
    document.getElementById("modalAwayName").textContent = match.awayTeam.name;

    document.getElementById("modalHomeAvatar").textContent = match.homeTeam.name.charAt(0);
    document.getElementById("modalAwayAvatar").textContent = match.awayTeam.name.charAt(0);

    document.getElementById("modalHomeScore").value = match.homeScore !== null && match.homeScore !== undefined ? match.homeScore : "";
    document.getElementById("modalAwayScore").value = match.awayScore !== null && match.awayScore !== undefined ? match.awayScore : "";

    document.getElementById("modalEditNote").style.display = isEdit ? "block" : "none";
    document.getElementById("scoreModal").style.display = "flex";
}

function closeScoreModal() {
    document.getElementById("scoreModal").style.display = "none";
}

async function submitMatchScore(event) {
    event.preventDefault();
    const matchId = document.getElementById("modalMatchId").value;
    const homeScore = parseInt(document.getElementById("modalHomeScore").value, 10);
    const awayScore = parseInt(document.getElementById("modalAwayScore").value, 10);

    if (isNaN(homeScore) || isNaN(awayScore) || homeScore < 0 || awayScore < 0) {
        showToast("Enter valid non-negative scores", "error");
        return;
    }

    try {
        await api(`/api/matches/${matchId}/result`, {
            method: "PUT",
            body: JSON.stringify({ homeScore, awayScore })
        });

        showToast("Result recorded & standings auto-updated without duplicate points!", "success");
        closeScoreModal();

        await loadMatches();
        await loadStandings();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    }
}

/* ==========================================================================
   Full Official Standings Table View
   ========================================================================== */

async function loadStandings() {
    try {
        const standings = await api("/api/standings");
        state.standings = standings || [];
        renderFullStandingsTable();
        updateDashboardShowcase();
    } catch (error) {
        showToast("Failed to load standings: " + error.message, "error");
    }
}

function renderFullStandingsTable() {
    const tbody = document.getElementById("fullStandingsBody");
    if (!tbody) return;

    if (!state.standings || state.standings.length === 0) {
        tbody.innerHTML = `<tr><td colspan="12" class="text-center text-muted py-5">No teams registered in standings yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = state.standings.map((entry, idx) => {
        const rank = idx + 1;
        const gd = entry.goalDifference > 0 ? `+${entry.goalDifference}` : entry.goalDifference;

        // Generate Form Dots (W/D/L)
        let formPills = "";
        for (let i = 0; i < Math.min(entry.won, 3); i++) formPills += `<span class="form-dot dot-w">W</span>`;
        for (let i = 0; i < Math.min(entry.drawn, 2); i++) formPills += `<span class="form-dot dot-d">D</span>`;
        for (let i = 0; i < Math.min(entry.lost, 2); i++) formPills += `<span class="form-dot dot-l">L</span>`;
        if (!formPills) formPills = `<span class="text-muted text-xs">-</span>`;

        return `
            <tr>
                <td class="text-center">
                    <span class="pos-pill ${rank === 1 ? 'pos-1' : (rank === 2 ? 'pos-2' : (rank === 3 ? 'pos-3' : 'pos-def'))}">
                        ${rank}
                    </span>
                </td>
                <td>
                    <strong class="cell-team-name">${escapeHtml(entry.team.name)}</strong>
                </td>
                <td class="text-muted text-xs">${escapeHtml(entry.team.department || "General")}</td>
                <td class="text-center">${entry.played}</td>
                <td class="text-center th-win" style="font-weight:700;">${entry.won}</td>
                <td class="text-center th-draw" style="font-weight:700;">${entry.drawn}</td>
                <td class="text-center th-loss" style="font-weight:700;">${entry.lost}</td>
                <td class="text-center">${entry.goalsFor}</td>
                <td class="text-center">${entry.goalsAgainst}</td>
                <td class="text-center" style="font-weight:700;">${gd}</td>
                <td class="text-center pts-cell">${entry.points}</td>
                <td class="text-center">
                    <div class="form-pill-group">${formPills}</div>
                </td>
            </tr>
        `;
    }).join("");
}

async function recalculateStandings() {
    try {
        const updated = await api("/api/standings/recalculate", { method: "POST" });
        state.standings = updated || [];
        showToast("Standings table recalculated successfully!", "success");
        renderFullStandingsTable();
        updateDashboardShowcase();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    }
}

/* ==========================================================================
   Scoring Rules Configuration
   ========================================================================== */

async function loadScoringRules() {
    try {
        const rule = await api("/api/standings/rules");
        if (rule) {
            state.scoringRule = rule;
            document.getElementById("winPointsInput").value = rule.winPoints;
            document.getElementById("drawPointsInput").value = rule.drawPoints;
            document.getElementById("lossPointsInput").value = rule.lossPoints;

            const summary = `Win: ${rule.winPoints} pts | Draw: ${rule.drawPoints} pt | Loss: ${rule.lossPoints} pts`;
            document.getElementById("standingsRuleSummary").textContent = summary;
        }
    } catch (error) {
        console.warn("Could not load scoring rules:", error);
    }
}

async function handleUpdateScoringRules(event) {
    event.preventDefault();
    const winPoints = parseInt(document.getElementById("winPointsInput").value, 10);
    const drawPoints = parseInt(document.getElementById("drawPointsInput").value, 10);
    const lossPoints = parseInt(document.getElementById("lossPointsInput").value, 10);

    try {
        const updated = await api("/api/standings/rules", {
            method: "PUT",
            body: JSON.stringify({ winPoints, drawPoints, lossPoints })
        });

        state.scoringRule = updated;
        showToast("Scoring rules saved & table auto-recalculated!", "success");

        await loadScoringRules();
        await loadStandings();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    }
}

/* ==========================================================================
   Live Activity Feed
   ========================================================================== */

async function loadActivities() {
    try {
        const activities = await api("/api/activities");
        const container = document.getElementById("dashActivityStream");
        if (!container) return;

        if (!activities || activities.length === 0) {
            container.innerHTML = `<div class="text-muted text-center text-xs py-3">No activity recorded yet.</div>`;
            return;
        }

        container.innerHTML = activities.slice(0, 5).map(act => {
            const timeStr = act.timestamp ? new Date(act.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : "";
            return `
                <div class="activity-item-pill">
                    <span style="color:#fff;">${escapeHtml(act.message)}</span>
                    <span style="color:var(--text-muted); font-size:10px; margin-left:6px;">${timeStr}</span>
                </div>
            `;
        }).join("");
    } catch (error) {
        console.warn("Could not load activities:", error);
    }
}

/* ==========================================================================
   Reset Actions
   ========================================================================== */

async function confirmResetMatchesOnly() {
    if (!confirm("Reset all tournament matches?\nTeams are preserved, standings points reset to 0.")) return;

    try {
        const res = await api("/api/matches/reset-matches", { method: "POST" });
        showToast(res?.message || "Matches cleared and standings reset", "info");

        await loadMatches();
        await loadStandings();
        await loadActivities();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function confirmResetTournament() {
    const input = prompt("DANGER: This will delete ALL teams, matches, and standings from MySQL.\n\nType 'RESET' to confirm:");
    if (input !== "RESET") {
        if (input !== null) showToast("Reset cancelled.", "info");
        return;
    }

    try {
        await api("/api/matches/reset", { method: "DELETE" });
        showToast("Tournament completely reset", "success");

        await loadTeams();
        await loadMatches();
        await loadStandings();
        await loadActivities();
        switchTab("tabDashboard");
    } catch (error) {
        showToast(error.message, "error");
    }
}

/* ==========================================================================
   Utility Functions
   ========================================================================== */

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* ==========================================================================
   Initialization
   ========================================================================== */

window.addEventListener("DOMContentLoaded", async () => {
    try {
        await Promise.all([
            loadScoringRules(),
            loadTeams(),
            loadMatches(),
            loadStandings(),
            loadActivities()
        ]);
    } catch (e) {
        console.error("Initialization error:", e);
    }
});
