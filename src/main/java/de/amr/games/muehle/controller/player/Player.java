package de.amr.games.muehle.controller.player;

import static java.lang.String.format;

import java.util.Optional;
import java.util.OptionalInt;

import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;

/**
 * Common interface for players.
 * 
 * @author Armin Reichert
 */
public interface Player {

	default String name() {
		return format("%s(%s)", getClass().getSimpleName(), color());
	}

	boolean isInteractive();

	MillGameModel model();

	StoneColor color();

	default boolean canJump() {
		return model().board.stoneCount(color()) == 3;
	}

	default boolean isTrapped() {
		return model().board.isTrapped(color());
	}

	OptionalInt supplyPlacingPosition();

	OptionalInt supplyRemovalPosition();

	void newMove();

	Optional<Move> supplyMove();
}