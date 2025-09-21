package com.app.playerservicejava.service;

import com.app.playerservicejava.model.*;
import com.app.playerservicejava.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
            Thread.sleep((long)(Math.random() * 2000));
        } catch (Exception e) {
            LOGGER.error("message=Exception in getPlayerById; exception={}", e.toString());
            return Optional.empty();
        }
        return player;
    }

    public CursorPaginatedPlayersResponse getPlayersCursorPaginated(String cursor, int limit, String direction) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        List<Player> players;
        String nextCursor = null;
        String previousCursor = null;

        PageRequest pageable = PageRequest.of(0, limit + 1); // +1 to check if there are more records

        if (cursor == null || cursor.trim().isEmpty()) {
            // First page
            players = playerRepository.findAllByOrderByPlayerIdAsc(pageable);
        } else {
            // Validate cursor exists
            if (!playerRepository.existsByPlayerId(cursor)) {
                throw new IllegalArgumentException("Invalid cursor: " + cursor);
            }

            if ("prev".equalsIgnoreCase(direction)) {
                // Previous page: get records before cursor in reverse order, then reverse the list
                List<Player> tempPlayers = playerRepository.findByPlayerIdLessThanOrderByPlayerIdDesc(cursor, pageable);
                Collections.reverse(tempPlayers);
                players = tempPlayers;
            } else {
                // Next page (default): get records after cursor
                players = playerRepository.findByPlayerIdGreaterThanOrderByPlayerIdAsc(cursor, pageable);
            }
        }

        // Check if there are more records and set cursors
        boolean hasMore = players.size() > limit;
        if (hasMore) {
            // Remove the extra record used for checking
            Player lastPlayer = players.remove(players.size() - 1);
            nextCursor = lastPlayer.getPlayerId();
        }

        // Set previous cursor if not first page
        if (cursor != null && !players.isEmpty()) {
            previousCursor = players.get(0).getPlayerId();
        }

        CursorPaginationMetadata metadata = new CursorPaginationMetadata(
            nextCursor,
            previousCursor,
            limit,
            hasMore,
            cursor != null && !"prev".equalsIgnoreCase(direction),
            playerRepository.count() // Optional
        );

        return new CursorPaginatedPlayersResponse(players, metadata);
    }

}
