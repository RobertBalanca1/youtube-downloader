package com.example.downloader.controller;

import com.example.downloader.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DownloadController {

    @Autowired
    private DownloadService downloadService;

    @PostMapping("/downloadPlaylist")
    public Map<String, Object> downloadPlaylist(@RequestBody Map<String, String> body) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String playlistUrl = body.get("playlist");
            String canal = body.get("canal");

            if (playlistUrl == null || playlistUrl.isBlank()) {
                resp.put("status", "error");
                resp.put("message", "playlistUrl es requerido");
                return resp;
            }

            String zipPath = downloadService.downloadPlaylist(playlistUrl, canal);
            resp.put("status", "success");
            resp.put("zipPath", zipPath);

        } catch (Exception e) {
            resp.put("status", "error");
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    @GetMapping("/progress")
    public Map<String, Object> getProgress() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("progress", downloadService.getProgress());
        return resp;
    }
}
