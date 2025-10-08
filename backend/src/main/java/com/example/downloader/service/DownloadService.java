package com.example.downloader.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DownloadService {

    private final AtomicInteger progress = new AtomicInteger(0);

    public int getProgress() {
        return progress.get();
    }

    public String downloadPlaylist(String playlistUrl, String canal) throws Exception {
        if (playlistUrl == null || playlistUrl.isBlank()) {
            throw new IllegalArgumentException("playlistUrl es requerido");
        }

        progress.set(0);
        Path tempDir = Files.createTempDirectory("youtube_mp3_");
        List<String> errorLog = Collections.synchronizedList(new ArrayList<>());

        String ytDlpPath = "C:\\Tools\\yt-dlp.exe";   // Ruta absoluta
        String ffmpegPath = "C:\\Tools\\ffmpeg.exe";  // Ruta absoluta

        // Verificar que existan los ejecutables
        if (!Files.exists(Paths.get(ytDlpPath))) throw new RuntimeException("yt-dlp.exe no encontrado en " + ytDlpPath);
        if (!Files.exists(Paths.get(ffmpegPath))) throw new RuntimeException("ffmpeg.exe no encontrado en " + ffmpegPath);

        // ── Extraer videos de la playlist ──
        List<String> videoUrls = extractVideoUrls(ytDlpPath, playlistUrl);
        if (videoUrls.isEmpty()) {
            FileUtils.deleteDirectory(tempDir.toFile());
            throw new Exception("❌ No se pudieron extraer videos válidos de la playlist.");
        }

        int totalVideos = videoUrls.size();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(totalVideos);

        for (int i = 0; i < totalVideos; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            ytDlpPath,
                            "-x",
                            "--audio-format", "mp3",
                            "--ffmpeg-location", ffmpegPath,
                            "-o", tempDir.toString() + "/%(title)s.%(ext)s",
                            videoUrls.get(idx)
                    );
                    pb.redirectErrorStream(true);
                    Process p = pb.start();

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("ERROR") || line.contains("unavailable")) {
                                errorLog.add("⚠️ Error descargando " + videoUrls.get(idx) + ": " + line);
                            }
                        }
                    }

                    p.waitFor();
                } catch (Exception e) {
                    errorLog.add("❌ Error en " + videoUrls.get(idx) + ": " + e.getMessage());
                } finally {
                    int done = totalVideos - (int) latch.getCount() + 1;
                    progress.set((int) (((double) done / totalVideos) * 100));
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // ── Crear ZIP final ──
        String artistName = (canal != null && !canal.isBlank()) ? canal.replaceAll("[^a-zA-Z0-9]", "_") : "UnknownArtist";
        String playlistName = "Playlist_" + System.currentTimeMillis();
        Path zipPath = Paths.get(System.getProperty("user.home"), "Downloads", artistName + "_" + playlistName + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                            Files.copy(file, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            errorLog.add("Error al comprimir: " + file + " -> " + e.getMessage());
                        }
                    });
        }

        // ── Guardar log ──
        Path logFile = Paths.get(System.getProperty("user.home"), "Downloads", artistName + "_" + playlistName + "_log.txt");
        Files.write(logFile, errorLog, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        FileUtils.deleteDirectory(tempDir.toFile());
        progress.set(100);
        return zipPath.toString();
    }

    private List<String> extractVideoUrls(String ytDlpPath, String playlistUrl) throws IOException, InterruptedException {
        List<String> urls = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(ytDlpPath, "--flat-playlist", "--dump-json", playlistUrl);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // Extraer URL directa
                    int idIndex = line.indexOf("\"id\":");
                    if (idIndex != -1) {
                        String idPart = line.substring(idIndex + 5);
                        String id = idPart.split("[\",]")[1];
                        urls.add("https://www.youtube.com/watch?v=" + id);
                    }
                } catch (Exception ignored) {}
            }
        }

        p.waitFor();
        return urls;
    }
}
