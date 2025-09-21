package com.app.playerservicejava.model;

import java.util.List;

public class CursorPaginatedPlayersResponse {
    private List<Player> players;
    private CursorPaginationMetadata pagination;

    public CursorPaginatedPlayersResponse() {}

    public CursorPaginatedPlayersResponse(List<Player> players, CursorPaginationMetadata pagination) {
        this.players = players;
        this.pagination = pagination;
    }

    // Getters and setters
    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public CursorPaginationMetadata getPagination() {
        return pagination;
    }

    public void setPagination(CursorPaginationMetadata pagination) {
        this.pagination = pagination;
    }
}
