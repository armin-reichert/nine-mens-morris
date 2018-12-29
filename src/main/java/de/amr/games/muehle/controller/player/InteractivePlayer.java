package de.amr.games.muehle.controller.player;

import static de.amr.games.muehle.model.board.Board.areNeighbors;
import static de.amr.games.muehle.model.board.Board.neighbor;
import static de.amr.games.muehle.model.board.Direction.EAST;
import static de.amr.games.muehle.model.board.Direction.NORTH;
import static de.amr.games.muehle.model.board.Direction.SOUTH;
import static de.amr.games.muehle.model.board.Direction.WEST;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.Direction;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer implements Player {

	private final EnumMap<Direction, Integer> steering = new EnumMap<>(Direction.class);
	private final MillGameModel model;
	private final StoneColor color;
	private final Move move;
	private BiFunction<Integer, Integer, OptionalInt> boardPositionFinder;

	public InteractivePlayer(MillGameModel model,
			BiFunction<Integer, Integer, OptionalInt> boardPositionFinder, StoneColor color) {
		this.model = model;
		this.boardPositionFinder = boardPositionFinder;
		this.color = color;
		this.steering.put(NORTH, VK_UP);
		this.steering.put(EAST, VK_RIGHT);
		this.steering.put(SOUTH, VK_DOWN);
		this.steering.put(WEST, VK_LEFT);
		this.move = new Move();
	}

	@Override
	public String name() {
		return Messages.text(color == WHITE ? "white" : "black");
	}

	@Override
	public boolean isInteractive() {
		return true;
	}

	@Override
	public MillGameModel model() {
		return model;
	}

	@Override
	public StoneColor color() {
		return color;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return boardPositionClicked();
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return boardPositionClicked();
	}

	@Override
	public void newMove() {
		move.clear();
	}

	@Override
	public Optional<Move> supplyMove() {
		if (!move.from().isPresent()) {
			boardPositionClicked().ifPresent(p -> {
				if (model.board.isEmptyPosition(p)) {
					Application.LOGGER.info(Messages.text("stone_at_position_not_existing", p));
				} else if (!model.board.hasStoneAt(p, color)) {
					Application.LOGGER.info(Messages.text("stone_at_position_wrong_color", p));
				} else if (!canJump() && !model.board.hasEmptyNeighbor(p)) {
					Application.LOGGER.info(Messages.text("stone_at_position_cannot_move", p));
				} else {
					move.setFrom(p);
					Application.LOGGER.info("Move starts from " + p);
				}
			});
		} else if (!move.to().isPresent()) {
			supplyMoveEndPosition().ifPresent(p -> move.setTo(p));
			if (move.to().isPresent() && model.board.isEmptyPosition(move.to().get())
					&& (canJump() || areNeighbors(move.from().get(), move.to().get()))) {
				Application.LOGGER.info("Move leads to " + move.to().get());
				return Optional.of(move);
			} else {
				move.clearTo();
			}
		}
		return Optional.empty();
	}

	private OptionalInt supplyMoveEndPosition() {
		if (!move.from().isPresent()) {
			return OptionalInt.empty();
		}
		int from = move.from().get();
		// if end position is uniquely determined, use it
		if (!canJump() && model.board.emptyNeighbors(from).count() == 1) {
			return model.board.emptyNeighbors(from).findFirst();
		}
		// if move direction has been specified, use position in that direction
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			return neighbor(from, optMoveDirection.get());
		}
		// use mouse click position if possible
		return boardPositionClicked();
	}

	private Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return steering.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	private OptionalInt boardPositionClicked() {
		return Mouse.clicked() ? boardPositionFinder.apply(Mouse.getX(), Mouse.getY())
				: OptionalInt.empty();
	}
}