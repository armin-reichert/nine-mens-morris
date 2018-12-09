package de.amr.games.muehle.rules;

import java.util.Optional;

import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Move;

public interface MovingRule extends Rule {

	Optional<Move> supplyMove(Player player);
}