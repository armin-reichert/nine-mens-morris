package de.amr.games.muehle.rules;

import java.util.OptionalInt;

import de.amr.games.muehle.controller.player.Player;

public interface PlacingRule extends Rule {

	OptionalInt supplyPlacingPosition(Player player);
}
