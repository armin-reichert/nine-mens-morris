package de.amr.games.muehle.controller.player;

import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.rules.MovingRule;
import de.amr.games.muehle.rules.MovingRules;
import de.amr.games.muehle.rules.PlacingRule;
import de.amr.games.muehle.rules.PlacingRules;
import de.amr.games.muehle.rules.RemovalRule;
import de.amr.games.muehle.rules.RemovalRules;

public class Zwick extends RuleBasedPlayer {

	static final PlacingRule[] PLACING_RULES = {
			/*@formatter:off*/
			PlacingRules.FIRST_STONE_RANDOM,
			PlacingRules.CLOSE_MILL,
			PlacingRules.DESTROY_MILL, 
			PlacingRules.OPEN_TWO_MILLS,
			PlacingRules.OPEN_ONE_MILL,
			PlacingRules.NEAR_OWN_COLOR, 
			PlacingRules.RANDOM,
			/*@formatter:on*/
	};

	static final MovingRule[] MOVING_RULES = {
			/*@formatter:off*/
			MovingRules.CAN_CLOSE_MILL,
			MovingRules.RANDOM,
			/*@formatter:on*/
	};

	static final RemovalRule[] REMOVAL_RULES = {
			/*@formatter:off*/
			RemovalRules.STONE_IN_OPEN_MILL,
			RemovalRules.STONE_WHICH_CAN_MOVE,
			RemovalRules.RANDOM_OUTSIDE_MILL,
			RemovalRules.RANDOM,
			/*@formatter:on*/
	};

	@Override
	public String name() {
		return String.format("Zwicki (%s)", Messages.text(color() == WHITE ? "white" : "black"));
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	public Zwick(MillGameModel model, StoneColor color) {
		super(model, color, PLACING_RULES, MOVING_RULES, REMOVAL_RULES);
	}
}