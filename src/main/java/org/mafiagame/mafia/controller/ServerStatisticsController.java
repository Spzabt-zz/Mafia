package org.mafiagame.mafia.controller;

import lombok.extern.slf4j.Slf4j;
import org.mafiagame.mafia.model.ServerStatistics;
import org.mafiagame.mafia.service.ServerStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1")
public class ServerStatisticsController {
    private final ServerStatisticsService serverStatisticsService;

    @Autowired
    public ServerStatisticsController(ServerStatisticsService serverStatisticsService) {
        this.serverStatisticsService = serverStatisticsService;
    }

    @PostMapping("/serverStatistics/add")
    public ResponseEntity<String> addServerStatistics(@RequestBody ServerStatistics serverStatistics) {
        log.info("Add server stats: {}", serverStatistics);
        serverStatisticsService.addServerStatistics(serverStatistics);
        return ResponseEntity.ok("Server stats added");
    }

    @GetMapping("/serverStatistics/get")
    public ResponseEntity<List<ServerStatistics>> getServerStatistics() {
        log.info("Get server stats");
        return ResponseEntity.ok(serverStatisticsService.getServerStatistics());
    }
}
