package de.amr.games.muehle.controller.player;

import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.rules.MovingRule;
import de.amr.games.muehle.rules.MovingRules;
import de.amr.games.muehle.rules.PlacingRule;
import de.amr.games.muehle.rules.PlacingRules;
import de.amr.games.muehle.rules.RemovalRule;
import de.amr.games.muehle.rules.RemovalRules;

/**
 * A player acting randomly.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer extends RuleBasedPlayer {

	public RandomPlayer(MillGameModel model, StoneColor color) {
		super(model, color, new PlacingRule[] { PlacingRules.RANDOM },
				new MovingRule[] { MovingRules.RANDOM },
				new RemovalRule[] { RemovalRules.RANDOM_OUTSIDE_MILL });
	}

	@Override
	public boolean isInteractive() {
		return false;
	}
}