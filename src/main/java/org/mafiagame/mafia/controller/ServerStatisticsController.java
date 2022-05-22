package org.mafiagame.mafia.controller;

import org.mafiagame.mafia.model.ServerStatistics;
import org.mafiagame.mafia.service.ServerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class ServerStatisticsController {
    private final ServerStatisticsService serverStatisticsService;

    @Autowired
    public ServerStatisticsController(ServerStatisticsService serverStatisticsService) {
        this.serverStatisticsService = serverStatisticsService;
    }

    @PostMapping("/serverStatistics/add")
    public ResponseEntity addServerStatistics(@RequestBody ServerStatistics serverStatistics) {
        try {
            serverStatisticsService.addServerStatistics(serverStatistics);
            return ResponseEntity.ok("Server stats added");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Some error - " + e.getMessage());
        }
    }

    @GetMapping("/serverStatistics/get")
    public ResponseEntity getVotes() {
        try {
            return ResponseEntity.ok("Everything is working! " + serverStatisticsService.getServerStatistics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Some error! " + e.getMessage());
        }
    }
}
