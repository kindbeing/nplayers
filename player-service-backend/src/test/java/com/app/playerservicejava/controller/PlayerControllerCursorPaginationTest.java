package com.app.playerservicejava.controller;

import com.app.playerservicejava.model.CursorPaginatedPlayersResponse;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.repository.PlayerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlayerControllerCursorPaginationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlayerRepository playerRepository;

    @LocalServerPort
    private int port;

    private String baseUrl() {
        return baseUrl("");
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/v1/players" + path;
    }

    @AfterEach
    void cleanup() {
        playerRepository.deleteAll();
    }

    @Test
    public void getPlayersCursorPaginated_firstPage_returnsFirst10Players() {
        // Arrange - create test data
        for (int i = 1; i <= 25; i++) {
            Player player = new Player();
            player.setPlayerId(String.format("player%03d", i));
            player.setFirstName("First" + i);
            player.setLastName("Last" + i);
            playerRepository.save(player);
        }

        // Act
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?limit=10"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).hasSize(10);

        // Check players are in correct order
        List<Player> players = response.getBody().getPlayers();
        assertThat(players.get(0).getPlayerId()).isEqualTo("player001");
        assertThat(players.get(9).getPlayerId()).isEqualTo("player010");

        // Check pagination metadata
        assertThat(response.getBody().getPagination().getNextCursor()).isEqualTo("player011");
        assertThat(response.getBody().getPagination().getPreviousCursor()).isNull();
        assertThat(response.getBody().getPagination().isHasNext()).isTrue();
        assertThat(response.getBody().getPagination().isHasPrevious()).isFalse();
        assertThat(response.getBody().getPagination().getLimit()).isEqualTo(10);
    }

    @Test
    public void getPlayersCursorPaginated_withCursor_returnsNextPage() {
        // Arrange - create test data
        for (int i = 1; i <= 25; i++) {
            Player player = new Player();
            player.setPlayerId(String.format("player%03d", i));
            player.setFirstName("First" + i);
            player.setLastName("Last" + i);
            playerRepository.save(player);
        }

        // Act - get second page using cursor
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?cursor=player010&limit=10"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).hasSize(10);

        // Check players are from the second page
        List<Player> players = response.getBody().getPlayers();
        assertThat(players.get(0).getPlayerId()).isEqualTo("player011");
        assertThat(players.get(9).getPlayerId()).isEqualTo("player020");

        // Check pagination metadata
        assertThat(response.getBody().getPagination().getNextCursor()).isEqualTo("player021");
        assertThat(response.getBody().getPagination().getPreviousCursor()).isEqualTo("player011");
        assertThat(response.getBody().getPagination().isHasNext()).isTrue();
        assertThat(response.getBody().getPagination().isHasPrevious()).isTrue();
    }

    @Test
    public void getPlayersCursorPaginated_lastPage_hasNoNextCursor() {
        // Arrange - create test data (exactly 15 players)
        for (int i = 1; i <= 15; i++) {
            Player player = new Player();
            player.setPlayerId(String.format("player%03d", i));
            player.setFirstName("First" + i);
            player.setLastName("Last" + i);
            playerRepository.save(player);
        }

        // Act - get second page
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?cursor=player010&limit=10"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).hasSize(5); // Only 5 players left

        // Check pagination metadata
        assertThat(response.getBody().getPagination().getNextCursor()).isNull();
        assertThat(response.getBody().getPagination().isHasNext()).isFalse();
    }

    @Test
    public void getPlayersCursorPaginated_withPreviousDirection_returnsPreviousPage() {
        // Arrange - create test data
        for (int i = 1; i <= 25; i++) {
            Player player = new Player();
            player.setPlayerId(String.format("player%03d", i));
            player.setFirstName("First" + i);
            player.setLastName("Last" + i);
            playerRepository.save(player);
        }

        // Act - navigate backwards from player020
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?cursor=player020&limit=10&direction=prev"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).hasSize(10);

        // Should get players before player020 in ascending order
        List<Player> players = response.getBody().getPlayers();
        assertThat(players.get(0).getPlayerId()).isEqualTo("player009");
        assertThat(players.get(9).getPlayerId()).isEqualTo("player018");
    }

    @Test
    public void getPlayersCursorPaginated_withInvalidCursor_returnsBadRequest() {
        // Act
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?cursor=invalid-cursor&limit=10"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getPlayersCursorPaginated_withLimitGreaterThan100_defaultsTo100() {
        // Arrange - create many players
        for (int i = 1; i <= 150; i++) {
            Player player = new Player();
            player.setPlayerId(String.format("player%03d", i));
            player.setFirstName("First" + i);
            player.setLastName("Last" + i);
            playerRepository.save(player);
        }

        // Act
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor?limit=150"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert - should be limited to 100
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPlayers()).hasSize(100);
        assertThat(response.getBody().getPagination().getLimit()).isEqualTo(100);
    }

    @Test
    public void getPlayersCursorPaginated_emptyDatabase_returnsEmptyList() {
        // Act
        ResponseEntity<CursorPaginatedPlayersResponse> response = restTemplate.exchange(
            baseUrl("/cursor"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<CursorPaginatedPlayersResponse>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).isEmpty();
        assertThat(response.getBody().getPagination().getNextCursor()).isNull();
        assertThat(response.getBody().getPagination().isHasNext()).isFalse();
    }
}
