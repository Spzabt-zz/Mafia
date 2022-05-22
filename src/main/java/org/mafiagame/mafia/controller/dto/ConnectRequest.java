package org.mafiagame.mafia.controller.dto;

import lombok.Data;
import org.mafiagame.mafia.model.Player;

import java.util.List;

@Data
public class ConnectRequest {
    private Player player;
}
