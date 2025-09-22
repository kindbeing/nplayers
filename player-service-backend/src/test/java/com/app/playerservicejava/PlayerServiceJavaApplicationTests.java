package com.app.playerservicejava;

import com.app.playerservicejava.controller.CreatePlayerRequest;
import com.app.playerservicejava.controller.PlayerController;
import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.Players;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerServiceJavaApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlayerController playerController;

    @Test
    void contextLoads() {
        assertThat(playerController).isNotNull();
    }

    private String baseUrl() {
        return baseUrl("");
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/v1/players" + path;
    }

    @Test
    void createPlayer_whenCalled_createsANewPlayer() {
        String firstName = "first name";
        String lastName = "last name";
        String email = "first@gmail.com";
        CreatePlayerRequest request = new CreatePlayerRequest(firstName, lastName, email);
        ResponseEntity<Player> response = restTemplate.postForEntity(baseUrl(), request, Player.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Player player = response.getBody();
        assertThat(player).isNotNull();
        assertThat(player.getFirstName()).isEqualTo("first name");
        assertThat(player.getLastName()).isEqualTo("last name");
        assertThat(player.getEmail()).isEqualTo("first@gmail.com");
    }

    @Test
    void createPlayer_whenPlayerUsesAnExistingEmail_throwsA409() {
        String firstName = "first name";
        String lastName = "last name";
        String email = "first2@gmail.com";
        CreatePlayerRequest request = new CreatePlayerRequest(firstName, lastName, email);
        ResponseEntity<Player> response1 = restTemplate.postForEntity(baseUrl(), request, Player.class);
        ResponseEntity<Player> response2 = restTemplate.postForEntity(baseUrl(), request, Player.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getPlayers_whenCalled_returnsAllThePlayers() {
        ResponseEntity<Players> response = restTemplate.getForEntity(baseUrl(), Players.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPlayers()).hasSizeGreaterThan(0);
    }

    @Test
    void getPlayerById_whenCalled_returnsPlayerDetails() {
        ResponseEntity<Player> response = restTemplate.getForEntity(baseUrl("/aardsda01"), Player.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("David");
        System.out.println(response.getBody());
        assertThat(response.getBody().getPlayerId()).isEqualTo("aardsda01");
        assertThat(response.getBody().getBirthYear()).isEqualTo("1981");
        assertThat(response.getBody().getBirthMonth()).isEqualTo("12");
        assertThat(response.getBody().getBirthDay()).isEqualTo("27");
        assertThat(response.getBody().getBirthCountry()).isEqualTo("USA");
        assertThat(response.getBody().getBirthState()).isEqualTo("CO");
        assertThat(response.getBody().getBirthCity()).isEqualTo("Denver");
        assertThat(response.getBody().getDeathYear()).isEqualTo(null);
        assertThat(response.getBody().getDeathMonth()).isEqualTo(null);
        assertThat(response.getBody().getDeathDay()).isEqualTo(null);
        assertThat(response.getBody().getDeathCountry()).isEqualTo(null);
        assertThat(response.getBody().getDeathState()).isEqualTo(null);
        assertThat(response.getBody().getDeathCity()).isEqualTo(null);
        assertThat(response.getBody().getFirstName()).isEqualTo("David");
        assertThat(response.getBody().getLastName()).isEqualTo("Aardsma");
        assertThat(response.getBody().getGivenName()).isEqualTo("David Allan");
        assertThat(response.getBody().getWeight()).isEqualTo("215");
        assertThat(response.getBody().getHeight()).isEqualTo("75");
        assertThat(response.getBody().getBats()).isEqualTo("R");
        assertThat(response.getBody().getThrowStats()).isEqualTo("R");
        assertThat(response.getBody().getDebut()).isEqualTo("2004-04-06");
        assertThat(response.getBody().getFinalGame()).isEqualTo("2015-08-23");
        assertThat(response.getBody().getRetroId()).isEqualTo("aardd001");
        assertThat(response.getBody().getBbrefId()).isEqualTo("aardsda01");
        assertThat(response.getBody().getEmail()).isEqualTo(null);
    }

    @Test
    void getPlayerById_whenCalledForANonExistingId_throwsA404() {
        ResponseEntity<Player> response = restTemplate.getForEntity(baseUrl("/non-present"), Player.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
