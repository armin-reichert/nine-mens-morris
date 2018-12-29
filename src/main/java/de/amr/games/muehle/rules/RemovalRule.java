package de.amr.games.muehle.rules;

import java.util.OptionalInt;

import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.StoneColor;

public interface RemovalRule extends Rule {

	OptionalInt supplyRemovalPosition(Player player, StoneColor removalColor);
}
