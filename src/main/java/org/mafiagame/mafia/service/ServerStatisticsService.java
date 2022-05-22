package org.mafiagame.mafia.service;

import org.mafiagame.mafia.model.ServerStatistics;
import org.mafiagame.mafia.repository.ServerStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerStatisticsService {
    private final ServerStatisticsRepository serverStatisticsRepository;

    @Autowired
    public ServerStatisticsService(ServerStatisticsRepository serverStatisticsRepository) {
        this.serverStatisticsRepository = serverStatisticsRepository;
    }

    public void addServerStatistics(ServerStatistics serverStatistics) {
        serverStatisticsRepository.add(serverStatistics);
    }

    public List<ServerStatistics> getServerStatistics() {
        return serverStatisticsRepository.serverStatistics();
    }
}
