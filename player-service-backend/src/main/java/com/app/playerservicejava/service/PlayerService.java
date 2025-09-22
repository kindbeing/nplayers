package com.app.playerservicejava.service;

import com.app.playerservicejava.controller.CreatePlayerRequest;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import com.app.playerservicejava.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Players getPlayers(String search, String birthYear, String country, int page, int size) {
        // Parameter validation
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Max page size

        // Get all players and apply filters
        List<Player> allPlayers = (List<Player>) playerRepository.findAll();

        List<Player> filteredPlayers = allPlayers.stream()
                .filter(player -> matchesSearch(player, search))
                .filter(player -> matchesBirthYear(player, birthYear))
                .filter(player -> matchesCountry(player, country))
                .collect(Collectors.toList());

        // Apply pagination
        int totalElements = filteredPlayers.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Player> paginatedPlayers = (startIndex < totalElements) ?
                filteredPlayers.subList(startIndex, endIndex) :
                List.of();

        // Create response
        Players players = new Players();
        players.getPlayers().addAll(paginatedPlayers);
        return players;
    }

    private boolean matchesSearch(Player player, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }

        String searchTerm = search.toLowerCase().trim();
        return (player.getFirstName() != null && player.getFirstName().toLowerCase().contains(searchTerm)) ||
               (player.getLastName() != null && player.getLastName().toLowerCase().contains(searchTerm));
    }

    private boolean matchesBirthYear(Player player, String birthYear) {
        if (birthYear == null || birthYear.trim().isEmpty()) {
            return true;
        }

        return player.getBirthYear() != null && player.getBirthYear().equals(birthYear.trim());
    }

    private boolean matchesCountry(Player player, String country) {
        if (country == null || country.trim().isEmpty()) {
            return true;
        }

        return player.getBirthCountry() != null &&
               player.getBirthCountry().toLowerCase().contains(country.toLowerCase().trim());
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

