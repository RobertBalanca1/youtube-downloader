const downloadBtn = document.getElementById("downloadBtn");
const progressBar = document.getElementById("progressBar");
const progressLabel = document.getElementById("progressLabel");
const statusDiv = document.getElementById("status");

let pollInterval = null;

downloadBtn.addEventListener("click", async () => {
    const canal = document.getElementById("canal").value.trim();
    const playlist = document.getElementById("playlist").value.trim();

    if (!playlist) { 
        statusDiv.textContent = "La URL de playlist es obligatoria."; 
        return; 
    }

    downloadBtn.disabled = true;
    statusDiv.textContent = "Iniciando...";
    progressBar.style.width = "0%";
    progressLabel.textContent = "Progreso: 0%";

    try {
        const res = await fetch("/api/downloadPlaylist", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ canal, playlist })
        });
        const data = await res.json();

        startPollingProgress();

        if (data.status === "success") {
            statusDiv.textContent = `✅ Descarga completa. ZIP: ${data.zipPath}`;
            progressBar.style.width = "100%";
            progressLabel.textContent = "Progreso: 100%";
        } else {
            statusDiv.textContent = `❌ Error: ${data.message}`;
            progressBar.style.width = "0%";
            progressLabel.textContent = "Progreso: 0%";
            stopPollingProgress();
            downloadBtn.disabled = false;
        }
    } catch (err) {
        statusDiv.textContent = "Error de red o servidor: " + (err.message || err);
        downloadBtn.disabled = false;
        stopPollingProgress();
    }
});

function startPollingProgress() {
    if (pollInterval) return;
    pollInterval = setInterval(async () => {
        try {
            const r = await fetch("/api/progress");
            const j = await r.json();
            const pct = Math.min(100, Math.max(0, Number(j.progress || 0)));
            progressBar.style.width = pct + "%";
            progressLabel.textContent = `Progreso: ${pct}%`;
            if (pct >= 100) {
                stopPollingProgress();
                downloadBtn.disabled = false;
            }
        } catch (e) {}
    }, 800);
}

function stopPollingProgress() {
    if (pollInterval) { clearInterval(pollInterval); pollInterval = null; }
}
