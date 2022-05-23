package org.mafiagame.mafia.controller.dto;

import lombok.Data;

@Data
public class CreateLobbyRequest {
    private String lobbyName;
    private String adminName;
}
