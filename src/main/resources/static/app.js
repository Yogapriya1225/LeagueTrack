async function api(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    const data = await response.json().catch(() => null);

    if (!response.ok) {
        throw new Error(data?.error || "Request failed");
    }

    return data;
}

async function loadTeams() {
    const teams = await api("/api/teams");
    document.getElementById("teamsBody").innerHTML = teams.map(team => `
        <tr>
            <td>${team.id}</td>
            <td>${escapeHtml(team.name)}</td>
            <td>${escapeHtml(team.captain || "-")}</td>
            <td><button class="small danger" onclick="deleteTeam(${team.id})">Delete</button></td>
        </tr>
    `).join("");
}

async function registerTeam(event) {
    event.preventDefault();

    const name = document.getElementById("teamName").value.trim();
    const captain = document.getElementById("captain").value.trim();
    const message = document.getElementById("teamMessage");

    try {
        await api("/api/teams", {
            method: "POST",
            body: JSON.stringify({ name, captain })
        });

        message.textContent = "Team registered successfully.";
        message.style.color = "green";
        document.getElementById("teamForm").reset();

        await loadTeams();
        await loadStandings();
    } catch (error) {
        message.textContent = error.message;
        message.style.color = "red";
    }
}

async function deleteTeam(id) {
    if (!confirm("Delete this team? Its related fixtures will also be deleted.")) return;

    try {
        await api(`/api/teams/${id}`, { method: "DELETE" });
        await loadTeams();
        await loadMatches();
        await loadStandings();
    } catch (error) {
        alert(error.message);
    }
}

async function generateFixtures() {
    const message = document.getElementById("fixtureMessage");

    try {
        const matches = await api("/api/matches/generate", { method: "POST" });
        message.textContent = `${matches.length} fixtures generated successfully.`;
        message.style.color = "green";

        await loadMatches();
        await loadStandings();
    } catch (error) {
        message.textContent = error.message;
        message.style.color = "red";
    }
}

async function loadMatches() {
    const matches = await api("/api/matches");
    document.getElementById("matchesBody").innerHTML = matches.map(match => {
        const completed = match.status === "COMPLETED";

        return `
            <tr>
                <td>${match.id}</td>
                <td>${match.roundNumber}</td>
                <td>${escapeHtml(match.homeTeam.name)}</td>
                <td>${escapeHtml(match.awayTeam.name)}</td>
                <td>
                    ${completed
                        ? `${match.homeScore} - ${match.awayScore}`
                        : `
                            <input class="score-input" id="home-${match.id}" type="number" min="0" placeholder="0">
                            -
                            <input class="score-input" id="away-${match.id}" type="number" min="0" placeholder="0">
                          `
                    }
                </td>
                <td class="${completed ? "completed" : "scheduled"}">
                    ${completed ? "Completed" : "Scheduled"}
                </td>
                <td>
                    ${completed
                        ? "-"
                        : `<button class="small" onclick="recordResult(${match.id})">Save Result</button>`
                    }
                </td>
            </tr>
        `;
    }).join("");
}

async function recordResult(id) {
    const homeScore = Number(document.getElementById(`home-${id}`).value);
    const awayScore = Number(document.getElementById(`away-${id}`).value);

    if (!Number.isInteger(homeScore) || !Number.isInteger(awayScore)
        || homeScore < 0 || awayScore < 0) {
        alert("Enter valid non-negative scores.");
        return;
    }

    try {
        await api(`/api/matches/${id}/result`, {
            method: "PUT",
            body: JSON.stringify({ homeScore, awayScore })
        });

        await loadMatches();
        await loadStandings();
    } catch (error) {
        alert(error.message);
    }
}

async function loadStandings() {
    const standings = await api("/api/matches/standings");

    document.getElementById("standingsBody").innerHTML =
        standings.map((row, index) => `
            <tr>
                <td>${index + 1}</td>
                <td>${escapeHtml(row.teamName)}</td>
                <td>${row.played}</td>
                <td>${row.wins}</td>
                <td>${row.draws}</td>
                <td>${row.losses}</td>
                <td><strong>${row.points}</strong></td>
            </tr>
        `).join("");
}

async function resetTournament() {
    if (!confirm("This will delete all teams, fixtures and results. Continue?")) return;

    try {
        await api("/api/matches/reset", { method: "DELETE" });
        await loadTeams();
        await loadMatches();
        await loadStandings();
    } catch (error) {
        alert(error.message);
    }
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

document.getElementById("teamForm").addEventListener("submit", registerTeam);

window.addEventListener("DOMContentLoaded", async () => {
    await loadTeams();
    await loadMatches();
    await loadStandings();
});
