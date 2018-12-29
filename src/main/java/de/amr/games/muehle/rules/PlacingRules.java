package de.amr.games.muehle.rules;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.util.TriFunction;

/**
 * Enumerates some placing rules.
 * 
 * @author Armin Reichert
 */
public enum PlacingRules implements PlacingRule {

	FIRST_STONE_RANDOM(
			"Setze Stein auf Position %d, weil noch kein Stein meiner Farbe gesetzt wurde",
			(board, player, color) -> randomElement(board.emptyPositions()),
			(board, player, color) -> board.positions(color).count() == 0),

	CLOSE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geschlossen wird",
			(board, player, color) -> randomElement(board.positionsClosingMill(color))),

	DESTROY_MILL(
			"Setze Stein auf Position %d, weil gegnerische Mühle verhindert wird",
			(board, player, color) -> randomElement(board.positionsClosingMill(color.other()))),

	OPEN_TWO_MILLS(
			"Setze Stein auf Position %d, weil zwei eigene Mühlen geöffnet werden",
			(board, player, color) -> randomElement(board.positionsOpeningTwoMills(color))),

	OPEN_ONE_MILL(
			"Setze Stein auf Position %d, weil eigene Mühle geöffnet wird",
			(board, player, color) -> randomElement(board.positionsOpeningMill(color))),

	NEAR_OWN_COLOR(
			"Setze Stein auf Position %d, weil es eine freie Position neben eigenem Stein ist",
			(board, player, color) -> {
				OptionalInt emptyNeighbor = randomElement(
						board.positions(color).filter(board::hasEmptyNeighbor));
				return emptyNeighbor.isPresent()
						? randomElement(board.emptyNeighbors(emptyNeighbor.getAsInt()))
						: OptionalInt.empty();
			}),

	RANDOM(
			"Setze Stein auf Position %d, weil kein Spezialfall zutraf",
			(board, player, color) -> randomElement(board.emptyPositions()))

	;

	@Override
	public OptionalInt supplyPlacingPosition(Player player) {
		return condition.apply(player.model().board, player, player.color())
				? positionSupplier.apply(player.model().board, player, player.color())
				: OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private PlacingRules(String description,
			TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier,
			TriFunction<Board, Player, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private PlacingRules(String description,
			TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, player, color) -> true);
	}

	private final String description;
	private final TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier;
	private final TriFunction<Board, Player, StoneColor, Boolean> condition;
}