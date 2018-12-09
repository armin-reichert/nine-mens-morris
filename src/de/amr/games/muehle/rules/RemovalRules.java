package de.amr.games.muehle.rules;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.util.TriFunction;

/**
 * Enumerates some removal rules.
 * 
 * @author Armin Reichert
 */
public enum RemovalRules implements RemovalRule {

	STONE_IN_OPEN_MILL(
			"Entferne Stein an Position %d, weil er Teil einer offenen Mühle ist",
			(board, player,
					color) -> randomElement(board.positions(color).filter(p -> !board.inMill(p, color))
							.filter(p -> board.isPartOfOpenMill(p, color)))),

	STONE_WHICH_CAN_MOVE(
			"Entferne Stein an Position %d, weil er bewegt werden kann",
			(board, player,
					color) -> randomElement(board.positions(color).filter(p -> !board.inMill(p, color))
							.filter(board::hasEmptyNeighbor))),

	RANDOM_OUTSIDE_MILL(
			"Entferne Stein außerhalb eine Mühle an Position %d",
			(board, player,
					color) -> randomElement(board.positions(color).filter(p -> !board.inMill(p, color)))),

	RANDOM(
			"Entferne Stein an Position %d",
			(board, player, color) -> randomElement(board.positions(color))),

	;

	@Override
	public OptionalInt supplyRemovalPosition(Player player, StoneColor removalColor) {
		return condition.apply(player.model().board, player, removalColor)
				? positionSupplier.apply(player.model().board, player, removalColor)
				: OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private RemovalRules(String description,
			TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier,
			TriFunction<Board, Player, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private RemovalRules(String description,
			TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, player, removalColor) -> true);
	}

	private final String description;
	private final TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier;
	private final TriFunction<Board, Player, StoneColor, Boolean> condition;
}