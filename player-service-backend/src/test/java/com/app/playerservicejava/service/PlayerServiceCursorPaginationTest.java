package com.app.playerservicejava.service;

import com.app.playerservicejava.model.CursorPaginatedPlayersResponse;
import com.app.playerservicejava.model.CursorPaginationMetadata;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceCursorPaginationTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player1, player2, player3, player4, player5;

    @BeforeEach
    void setUp() {
        player1 = createPlayer("player001");
        player2 = createPlayer("player002");
        player3 = createPlayer("player003");
        player4 = createPlayer("player004");
        player5 = createPlayer("player005");
    }

    private Player createPlayer(String playerId) {
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setFirstName("First");
        player.setLastName("Last");
        return player;
    }

    @Test
    public void getPlayersCursorPaginated_firstPage_returnsFirstPlayersWithNextCursor() {
        // Arrange
        List<Player> players = new ArrayList<>(Arrays.asList(player1, player2, player3, player4, player5, player1)); // 6th is extra
        when(playerRepository.findAllByOrderByPlayerIdAsc(any(PageRequest.class))).thenReturn(players);
        when(playerRepository.count()).thenReturn(100L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(null, 5, "next");

        // Assert
        assertThat(response.getPlayers()).hasSize(5);
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo("player001");
        assertThat(response.getPlayers().get(4).getPlayerId()).isEqualTo("player005");

        CursorPaginationMetadata pagination = response.getPagination();
        assertThat(pagination.getNextCursor()).isEqualTo("player001"); // 6th player
        assertThat(pagination.getPreviousCursor()).isNull();
        assertThat(pagination.isHasNext()).isTrue();
        assertThat(pagination.isHasPrevious()).isFalse();
        assertThat(pagination.getLimit()).isEqualTo(5);
        assertThat(pagination.getTotalElements()).isEqualTo(100L);
    }

    @Test
    public void getPlayersCursorPaginated_withCursor_returnsNextPage() {
        // Arrange
        List<Player> players = new ArrayList<>(Arrays.asList(player3, player4, player5, player1)); // 4th is extra
        when(playerRepository.existsByPlayerId("player002")).thenReturn(true);
        when(playerRepository.findByPlayerIdGreaterThanOrderByPlayerIdAsc(eq("player002"), any(PageRequest.class)))
            .thenReturn(players);
        when(playerRepository.count()).thenReturn(100L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated("player002", 3, "next");

        // Assert
        assertThat(response.getPlayers()).hasSize(3);
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo("player003");
        assertThat(response.getPlayers().get(2).getPlayerId()).isEqualTo("player005");

        CursorPaginationMetadata pagination = response.getPagination();
        assertThat(pagination.getNextCursor()).isEqualTo("player001");
        assertThat(pagination.getPreviousCursor()).isEqualTo("player003");
        assertThat(pagination.isHasNext()).isTrue();
        assertThat(pagination.isHasPrevious()).isTrue();
    }

    @Test
    public void getPlayersCursorPaginated_previousDirection_returnsPreviousPage() {
        // Arrange
        List<Player> tempPlayers = new ArrayList<>(Arrays.asList(player5, player4, player3, player2)); // Reverse order
        when(playerRepository.existsByPlayerId("player003")).thenReturn(true);
        when(playerRepository.findByPlayerIdLessThanOrderByPlayerIdDesc(eq("player003"), any(PageRequest.class)))
            .thenReturn(tempPlayers);
        when(playerRepository.count()).thenReturn(100L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated("player003", 3, "prev");

        // Assert
        assertThat(response.getPlayers()).hasSize(3);
        // Should be reversed back to ascending order
        assertThat(response.getPlayers().get(0).getPlayerId()).isEqualTo("player002");
        assertThat(response.getPlayers().get(1).getPlayerId()).isEqualTo("player003");
        assertThat(response.getPlayers().get(2).getPlayerId()).isEqualTo("player004");
    }

    @Test
    public void getPlayersCursorPaginated_lastPage_hasNoNextCursor() {
        // Arrange
        List<Player> players = new ArrayList<>(Arrays.asList(player4, player5)); // Only 2 players, no extra
        when(playerRepository.findAllByOrderByPlayerIdAsc(any(PageRequest.class))).thenReturn(players);
        when(playerRepository.count()).thenReturn(5L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(null, 5, "next");

        // Assert
        assertThat(response.getPlayers()).hasSize(2);
        CursorPaginationMetadata pagination = response.getPagination();
        assertThat(pagination.getNextCursor()).isNull();
        assertThat(pagination.isHasNext()).isFalse();
    }

    @Test
    public void getPlayersCursorPaginated_invalidCursor_throwsException() {
        // Arrange
        when(playerRepository.existsByPlayerId("invalid")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> playerService.getPlayersCursorPaginated("invalid", 10, "next"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid cursor: invalid");
    }

    @Test
    public void getPlayersCursorPaginated_limitTooHigh_defaultsTo100() {
        // Arrange
        List<Player> players = new ArrayList<>(Collections.nCopies(101, player1)); // 101 players
        when(playerRepository.findAllByOrderByPlayerIdAsc(any(PageRequest.class))).thenReturn(players);
        when(playerRepository.count()).thenReturn(1000L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(null, 150, "next");

        // Assert
        assertThat(response.getPlayers()).hasSize(100);
        assertThat(response.getPagination().getLimit()).isEqualTo(100);
    }

    @Test
    public void getPlayersCursorPaginated_limitTooLow_defaultsTo10() {
        // Arrange
        List<Player> players = new ArrayList<>(Arrays.asList(player1, player2, player3, player4, player5, player1)); // 6 players returned
        when(playerRepository.findAllByOrderByPlayerIdAsc(any(PageRequest.class))).thenReturn(players);
        when(playerRepository.count()).thenReturn(100L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(null, 0, "next");

        // Assert
        assertThat(response.getPlayers()).hasSize(6); // All players returned since we have less than limit (10) + 1
        assertThat(response.getPagination().getLimit()).isEqualTo(10);
    }

    @Test
    public void getPlayersCursorPaginated_emptyResult_hasNoCursors() {
        // Arrange
        when(playerRepository.findAllByOrderByPlayerIdAsc(any(PageRequest.class)))
            .thenReturn(Collections.emptyList());
        when(playerRepository.count()).thenReturn(0L);

        // Act
        CursorPaginatedPlayersResponse response = playerService.getPlayersCursorPaginated(null, 10, "next");

        // Assert
        assertThat(response.getPlayers()).isEmpty();
        CursorPaginationMetadata pagination = response.getPagination();
        assertThat(pagination.getNextCursor()).isNull();
        assertThat(pagination.getPreviousCursor()).isNull();
        assertThat(pagination.isHasNext()).isFalse();
        assertThat(pagination.isHasPrevious()).isFalse();
    }
}
