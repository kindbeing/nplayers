package com.app.playerservicejava.service;

import com.app.playerservicejava.controller.CreatePlayerRequest;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    public Players getPlayers() {
        Players players = new Players();
        playerRepository.findAll()
                .forEach(players.getPlayers()::add);
        return players;
    }

    public Optional<Player> getPlayerById(String playerId) {
        Optional<Player> player = null;

        /* simulated network delay */
        try {
            player = playerRepository.findById(playerId);
//            Thread.sleep((long) (Math.random() * 2000));
        } catch (Exception e) {
            LOGGER.error("message=Exception in getPlayerById; exception={}", e.toString());
            return Optional.empty();
        }
        return player;
    }

    public Player createPlayer(CreatePlayerRequest request) {
        Player withExistingEmail;
        try {
            withExistingEmail = playerRepository.findByEmail(request.email());
        } catch (Exception e) {
            throw new PSCustomException(e.getMessage());
        }

        if (withExistingEmail != null) {
            throw new PSDuplicateEmailException("Email address already exists");
        }

        Player toBeSaved = new Player(request.firstName(), request.lastName(), request.email());
        try {
            return playerRepository.save(toBeSaved);
        } catch (Exception e) {
            throw new PSCustomException(e.getMessage());
        }
    }
}

