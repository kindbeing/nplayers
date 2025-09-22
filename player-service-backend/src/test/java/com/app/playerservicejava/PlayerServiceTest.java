package com.app.playerservicejava;

import com.app.playerservicejava.controller.CreatePlayerRequest;
import com.app.playerservicejava.controller.PlayerController;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.repository.PlayerRepository;
import com.app.playerservicejava.service.PSCustomException;
import com.app.playerservicejava.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository repository;

    @InjectMocks
    private PlayerService service;

    @Test
    void createPlayer_whenRepositoryFails_throwsAnError() {
        when(repository.save(any(Player.class))).thenThrow(new DataAccessResourceFailureException("Access Failed"));
        CreatePlayerRequest p = new CreatePlayerRequest("", "", "");
        Exception e = assertThrows(PSCustomException.class, () -> service.createPlayer(p));
        assertThat(e.getMessage()).isEqualTo("Access Failed");
    }

    @Test
    void createPlayer_whenRepositoryFailsOnEmailCheck_throwsAnError() {
        when(repository.findByEmail(any(String.class))).thenThrow(new DataAccessResourceFailureException("Access Failed"));
        CreatePlayerRequest p = new CreatePlayerRequest("", "", "");
        Exception e = assertThrows(PSCustomException.class, () -> service.createPlayer(p));
        assertThat(e.getMessage()).isEqualTo("Access Failed");
    }
}
