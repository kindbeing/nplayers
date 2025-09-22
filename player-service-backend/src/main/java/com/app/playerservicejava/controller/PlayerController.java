package com.app.playerservicejava.controller;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.service.PlayerService;
import com.app.playerservicejava.service.chat.ChatClientService;
import io.github.ollama4j.exceptions.OllamaBaseException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "v1/players", produces = { MediaType.APPLICATION_JSON_VALUE })
public class PlayerController {
    @Resource
    private PlayerService playerService;

    @Autowired
    private ChatClientService chatClientService;

    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody CreatePlayerRequest request) {
        Player player = playerService.createPlayer(request);
        return ok(player);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Players> getPlayers() {
        Players players = playerService.getPlayers();
        return ok(players);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") String id) {
        Optional<Player> player = playerService.getPlayerById(id);

        if (player.isPresent()) {
            return new ResponseEntity<>(player.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzePlayer(@PathVariable("id") String id) {
        try {
            // Get player data first
            Optional<Player> playerOpt = playerService.getPlayerById(id);
            if (!playerOpt.isPresent()) {
                return new ResponseEntity<>("Player not found", HttpStatus.NOT_FOUND);
            }

            Player player = playerOpt.get();

            // Format player data for AI analysis
            String playerData = String.format(
                "Player: %s %s, Born: %s (%s years old), Physical: %s inches, %s lbs, Batting: %s, Throwing: %s, Career: %s to %s",
                player.getFirstName(),
                player.getLastName(),
                player.getBirthYear(),
                player.getBirthYear() != null ? (java.time.Year.now().getValue() - Integer.parseInt(player.getBirthYear())) : "unknown",
                player.getHeight(),
                player.getWeight(),
                player.getBats(),
                player.getThrowStats(),
                player.getDebut(),
                player.getFinalGame()
            );

            // Get AI analysis
            String analysis = chatClientService.analyzePlayer(playerData);

            return new ResponseEntity<>(analysis, HttpStatus.OK);

        } catch (OllamaBaseException | IOException | InterruptedException e) {
            return new ResponseEntity<>("AI analysis unavailable: " + e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            return new ResponseEntity<>("Analysis failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
