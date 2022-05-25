package org.mafiagame.mafia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MafiaGameService {
    private LobbyService lobbyService;

    @Autowired
    public MafiaGameService(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }


}
