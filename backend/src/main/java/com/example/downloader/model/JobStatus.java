package com.example.downloader.model;

/**
 * Objeto que representa el estado de la descarga para frontend
 */
public class JobStatus {
    private int progress;          // Porcentaje completado
    private String currentSong;    // Canción actual en proceso
    private String zipPath;        // Ruta del ZIP final
    private String logPath;        // Ruta del log de errores
    private String message;        // Mensaje general

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getCurrentSong() { return currentSong; }
    public void setCurrentSong(String currentSong) { this.currentSong = currentSong; }

    public String getZipPath() { return zipPath; }
    public void setZipPath(String zipPath) { this.zipPath = zipPath; }

    public String getLogPath() { return logPath; }
    public void setLogPath(String logPath) { this.logPath = logPath; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
