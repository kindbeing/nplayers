package com.app.playerservicejava.repository;
import com.app.playerservicejava.model.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, String> {

    // For cursor-based pagination
    List<Player> findByPlayerIdGreaterThanOrderByPlayerIdAsc(String playerId, Pageable pageable);
    List<Player> findByPlayerIdLessThanOrderByPlayerIdDesc(String playerId, Pageable pageable);
    List<Player> findAllByOrderByPlayerIdAsc(Pageable pageable);

    // For cursor validation
    boolean existsByPlayerId(String playerId);
}
