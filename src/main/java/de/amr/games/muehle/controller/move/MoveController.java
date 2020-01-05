package de.amr.games.muehle.controller.move;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.muehle.controller.move.MoveEvent.GOT_MOVE_FROM_PLAYER;
import static de.amr.games.muehle.controller.move.MoveState.ANIMATION;
import static de.amr.games.muehle.controller.move.MoveState.COMPLETE;
import static de.amr.games.muehle.controller.move.MoveState.JUMPING;
import static de.amr.games.muehle.controller.move.MoveState.READING_MOVE;
import static de.amr.games.muehle.model.board.Board.areNeighbors;
import static de.amr.games.muehle.model.board.Board.getDirection;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Direction;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.MillGameUI;
import de.amr.games.muehle.view.Stone;
import de.amr.statemachine.core.EventMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * A finite state machine for controlling the animated movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveController {

	private final StateMachine<MoveState, MoveEvent> fsm;
	private final Player player;
	private final MillGameUI gameUI;
	private final float moveTimeSec;
	private Move move;

	public MoveController(Player player, MillGameUI gameUI, float moveTimeSec) {
		this.player = player;
		this.gameUI = gameUI;
		this.moveTimeSec = moveTimeSec;
		this.move = null;
		this.fsm = buildStateMachine();
	}

	public StateMachine<MoveState, MoveEvent> getFsm() {
		return fsm;
	}

	private StateMachine<MoveState, MoveEvent> buildStateMachine() {
		//@formatter:off
		return StateMachine.beginStateMachine(MoveState.class, MoveEvent.class, EventMatchStrategy.BY_EQUALITY)
				.description("Move Control")
				.initialState(READING_MOVE)
				
				.states()
				
					.state(READING_MOVE)
						.onEntry(this::newMove)
						
					.state(READING_MOVE)
						.onTick(this::requestMoveFromPlayer)
							
					.state(ANIMATION)
						.onEntry(this::startAnimation)
						.onTick(this::updateAnimation)
						.onExit(this::stopAnimation)

					.state(JUMPING)
						.onEntry(() -> {
							LOGGER.info(player.name() + ": "
									+ Messages.text("jumping_from_to", move.from().get(), move.to().get()));
						})

					.state(COMPLETE)
						.onEntry(() -> gameUI.moveStone(move))
				
				.transitions()
				
					.when(READING_MOVE).then(JUMPING)
						.on(GOT_MOVE_FROM_PLAYER)
						.condition(player::canJump)
					
					.when(READING_MOVE).then(ANIMATION)
						.on(GOT_MOVE_FROM_PLAYER)
						
					.when(ANIMATION).then(COMPLETE)
						.condition(this::isMoveTargetReached)
						
					.when(JUMPING).then(COMPLETE)
				
				.endStateMachine();
		//@formatter:on
	}

	public Optional<Move> getMove() {
		return Optional.ofNullable(move);
	}

	private void newMove() {
		move = null;
		player.newMove();
	}

	private void requestMoveFromPlayer() {
		player.supplyMove().ifPresent(move -> {
			if (isMovePossible(move)) {
				this.move = move;
				fsm.enqueue(GOT_MOVE_FROM_PLAYER);
			}
		});
	}

	private void startAnimation() {
		if (move.isPresent()) {
			int from = move.from().get(), to = move.to().get();
			gameUI.getStoneAt(from).ifPresent(stone -> {
				float speed = Vector2f.euclideanDist(gameUI.getLocation(from), gameUI.getLocation(to))
						/ app().clock().sec(moveTimeSec);
				Direction dir = getDirection(from, to).get();
				if (dir == Direction.NORTH) {
					stone.tf.setVelocity(0, -speed);
				} else if (dir == Direction.EAST) {
					stone.tf.setVelocity(speed, 0);
				} else if (dir == Direction.SOUTH) {
					stone.tf.setVelocity(0, speed);
				} else if (dir == Direction.WEST) {
					stone.tf.setVelocity(-speed, 0);
				}
				LOGGER.info(player.name() + ": " + Messages.text("moving_from_to_towards", from, to, dir));
			});
		}
	}

	private void updateAnimation() {
		move.from().ifPresent(from -> gameUI.getStoneAt(from).ifPresent(stone -> stone.tf.move()));
	}

	private void stopAnimation() {
		move.from().ifPresent(from -> gameUI.getStoneAt(from).ifPresent(stone -> stone.tf.setVelocity(0, 0)));
	}

	private boolean isMoveTargetReached() {
		if (move.isPresent()) {
			int from = move.from().get(), to = move.to().get();
			Optional<Stone> optStone = gameUI.getStoneAt(from);
			if (optStone.isPresent()) {
				Stone stone = optStone.get();
				float speed = stone.tf.getVelocity().length();
				Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed,
						2 * speed);
				Vector2f toCenter = gameUI.getLocation(to);
				return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
			}
		}
		return false;
	}

	private boolean isMovePossible(Move move) {
		if (!move.isPresent()) {
			return false;
		}
		int from = move.from().get(), to = move.to().get();
		Optional<Stone> optStone = gameUI.getStoneAt(from);
		if (!optStone.isPresent()) {
			LOGGER.info(Messages.text("stone_at_position_not_existing", from));
			return false;
		}
		Stone stone = optStone.get();
		if (stone.getColor() != player.color()) {
			LOGGER.info(Messages.text("stone_at_position_wrong_color", from));
			return false;
		}
		if (!player.model().board.isEmptyPosition(to)) {
			LOGGER.info(Messages.text("stone_at_position", to));
			return false;
		}
		if (player.canJump()) {
			return true;
		}
		if (!areNeighbors(from, to)) {
			LOGGER.info(Messages.text("not_neighbors", from, to));
			return false;
		}
		return true;
	}
}