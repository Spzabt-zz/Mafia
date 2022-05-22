package org.mafiagame.mafia.service;

import lombok.AllArgsConstructor;
import org.mafiagame.mafia.model.MafiaGame;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MafiaGameService {
    private LobbyService lobbyService;

//    public MafiaGame createLobby() {
//        MafiaGame mafiaGame = new MafiaGame();
//        //mafiaGame.setLobby();
//
//        return mafiaGame;
//    }
}
