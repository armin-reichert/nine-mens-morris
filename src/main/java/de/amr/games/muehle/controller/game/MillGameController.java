package de.amr.games.muehle.controller.game;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.controller.game.MillGameEvent.STONE_REMOVED;
import static de.amr.games.muehle.controller.game.MillGameState.GAME_OVER;
import static de.amr.games.muehle.controller.game.MillGameState.MOVING;
import static de.amr.games.muehle.controller.game.MillGameState.MOVING_REMOVING;
import static de.amr.games.muehle.controller.game.MillGameState.PLACING;
import static de.amr.games.muehle.controller.game.MillGameState.PLACING_REMOVING;
import static de.amr.games.muehle.controller.game.MillGameState.STARTING;
import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import java.awt.event.KeyEvent;
import java.util.OptionalInt;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.easy.game.view.VisualController;
import de.amr.games.muehle.MillGameApp;
import de.amr.games.muehle.controller.move.MoveController;
import de.amr.games.muehle.controller.move.MoveState;
import de.amr.games.muehle.controller.player.InteractivePlayer;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.controller.player.Zwick;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.Assistant;
import de.amr.games.muehle.view.MillGameScene;
import de.amr.games.muehle.view.MillGameUI;
import de.amr.statemachine.core.EventMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Controller for the mill game application.
 * 
 * @author Armin Reichert
 */
public class MillGameController implements VisualController {

	public final MillGameApp app;
	public final MillGameModel model;
	public final Assistant assistant;
	private final StateMachine<MillGameState, MillGameEvent> fsm;
	private MillGameUI view;
	private Player whitePlayer;
	private Player blackPlayer;
	private Player turn;
	private Player assistedPlayer;
	private MoveController moveControl;
	private float moveTimeSeconds;
	private float placingTimeSeconds;
	private OptionalInt positionNearMouse;

	public MillGameController(MillGameApp app, MillGameModel model) {
		this.app = app;
		this.model = model;
		this.assistant = new Assistant(this);
		this.fsm = buildStateMachine();
		this.moveTimeSeconds = 0.75f;
		this.placingTimeSeconds = 1.5f;
		this.positionNearMouse = OptionalInt.empty();
	}

	private StateMachine<MillGameState, MillGameEvent> buildStateMachine() {
		//@formatter:off
		return StateMachine.beginStateMachine(MillGameState.class, MillGameEvent.class, EventMatchStrategy.BY_EQUALITY)

				.description("MillGameControl")
				.initialState(STARTING)

				.states()
				
					.state(STARTING)
						.onEntry(this::resetGame)

					.state(PLACING)
						.onTick(this::tryToPlaceStone)
					
					.state(PLACING_REMOVING)
						.onEntry(this::startRemoving)
					  .onTick(this::tryToRemoveStone)

					.state(MOVING)
						.onTick(this::updateMove)

					.state(MOVING_REMOVING)
						.onEntry(this::startRemoving)
						.onTick(this::tryToRemoveStone)

					.state(GAME_OVER)
						.onEntry(this::onGameOver)
						
				.transitions()

					.when(STARTING).then(PLACING)

					.when(PLACING).then(PLACING_REMOVING)
						.on(STONE_PLACED_IN_MILL)
						.act(this::onMillClosedByPlacing)

					.when(PLACING).then(MOVING)
						.on(STONE_PLACED)
						.condition(this::areAllStonesPlaced)
						.act(this::switchMoving)
						
					.stay(PLACING)
						.on(STONE_PLACED)
						.act(this::switchPlacing)

					.when(PLACING_REMOVING).then(MOVING)
						.on(STONE_REMOVED)
						.condition(this::areAllStonesPlaced)
						.act(this::switchMoving)

					.when(PLACING_REMOVING).then(PLACING)
						.on(STONE_REMOVED)
						.act(this::switchPlacing)

					.when(MOVING).then(GAME_OVER)
						.condition(this::isGameOver)

					.when(MOVING).then(MOVING_REMOVING)
						.condition(this::isMillClosedByMove)

					.stay(MOVING)
						.condition(this::isMoveComplete)
						.act(this::switchMoving)

					.when(MOVING_REMOVING).then(MOVING)
						.on(STONE_REMOVED)
						.act(this::switchMoving)

					.when(GAME_OVER).then(STARTING)
						.act(this::shallStartNewGame)
		
		.endStateMachine();
		//@formatter:off
	}

	public StateMachine<MillGameState, MillGameEvent> getFsm() {
		return fsm;
	}
	
	public Player whitePlayer() {
		return whitePlayer;
	}

	public void setWhitePlayer(Player whitePlayer) {
		this.whitePlayer = whitePlayer;
	}

	public Player blackPlayer() {
		return blackPlayer;
	}

	public void setBlackPlayer(Player blackPlayer) {
		this.blackPlayer = blackPlayer;
	}

	public void setView(MillGameUI view) {
		this.view = view;
		assistant.setView(view);
	}

	@Override
	public View currentView() {
		return view;
	}

	public void setMoveTimeSeconds(float moveTimeSeconds) {
		this.moveTimeSeconds = moveTimeSeconds;
	}

	public void setPlacingTimeSeconds(float placingTimeSeconds) {
		this.placingTimeSeconds = placingTimeSeconds;
	}

	public void setAssistedPlayer(Player assistedPlayer) {
		this.assistedPlayer = assistedPlayer;
	}

	public OptionalInt getPositionNearMouse() {
		return positionNearMouse;
	}

	@Override
	public void init() {
		MillGameScene gameScene = new MillGameScene(app, this);
		setWhitePlayer(new InteractivePlayer(model, gameScene::findBoardPosition, WHITE));
		setBlackPlayer(new Zwick(model, BLACK));
		setView(gameScene);
		gameScene.init();
		fsm.init();
		assistant.init();
		assistedPlayer = whitePlayer;
	}

	@Override
	public void update() {
		readUserInput();
		fsm.update();
		assistant.update();
	}

	private void readUserInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistant.toggle();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			assistant.setHelpLevel(Assistant.HelpLevel.NORMAL);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			assistant.setHelpLevel(Assistant.HelpLevel.HIGH);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_W)) {
			if (whitePlayer.isInteractive()) {
				setAssistedPlayer(whitePlayer);
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			if (blackPlayer.isInteractive()) {
				setAssistedPlayer(blackPlayer);
			}
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			view.toggleBoardPositionNumbers();
		}
		if (Mouse.moved()) {
			positionNearMouse = view.findBoardPosition(Mouse.getX(), Mouse.getY());
		}
	}

	private void announceWinner(Player winner) {
		view.showMessage("wins", winner.name());
		assistant.tellWin(winner);
	}

	public Player playerInTurn() {
		return turn;
	}

	public Player playerNotInTurn() {
		return turn == whitePlayer ? blackPlayer : whitePlayer;
	}

	private void turnPlacingTo(Player player) {
		turn = player;
		view.showMessage("must_place", turn.name());
	}

	private void turnMovingTo(Player player) {
		turn = player;
		moveControl = new MoveController(turn, view, moveTimeSeconds);
		moveControl.getFsm().traceTo(LOGGER, app().clock::getFrequency);
		moveControl.getFsm().init();
		view.showMessage("must_move", turn.name());
	}

	// implement methods from base class

	protected boolean areAllStonesPlaced() {
		return model.blackStonesPlaced == 9;
	}

	protected void switchPlacing() {
		turnPlacingTo(playerNotInTurn());
		if (!turn.isInteractive()) {
//			pause(Application.clock.sec(placingTimeSeconds));
		}
	}

	protected void onMillClosedByPlacing() {
		if (assistedPlayer == turn) {
			assistant.tellMillClosed();
		}
	}

	protected void switchMoving() {
		turnMovingTo(playerNotInTurn());
	}

	protected void tryToPlaceStone() {
		if (assistedPlayer == turn) {
			assistant.givePlacingHint(assistedPlayer);
		}
		turn.supplyPlacingPosition().ifPresent(placedAt -> {
			if (model.board.isEmptyPosition(placedAt)) {
				StoneColor placedColor = turn.color();
				view.putStoneAt(placedAt, placedColor);
				if (turn == whitePlayer) {
					model.whiteStonesPlaced += 1;
				} else {
					model.blackStonesPlaced += 1;
				}
				if (model.board.inMill(placedAt, placedColor)) {
					fsm.enqueue(STONE_PLACED_IN_MILL);
				} else {
					fsm.enqueue(STONE_PLACED);
				}
			} else {
				LOGGER.info(Messages.text("stone_at_position", placedAt));
			}
		});
	}

	protected void tryToRemoveStone() {
		turn.supplyRemovalPosition().ifPresent(p -> {
			StoneColor colorToRemove = playerNotInTurn().color();
			if (model.board.isEmptyPosition(p)) {
				LOGGER.info(Messages.text("stone_at_position_not_existing", p));
			} else if (model.board.getStoneAt(p).get() != colorToRemove) {
				LOGGER.info(Messages.text("stone_at_position_wrong_color", p));
			} else if (model.board.inMill(p, colorToRemove)
					&& !model.board.allStonesInMills(colorToRemove)) {
				LOGGER.info(Messages.text("stone_cannot_be_removed_from_mill"));
			} else {
				view.removeStoneAt(p);
				fsm.enqueue(STONE_REMOVED);
				LOGGER.info(Messages.text("removed_stone_at_position", turn.name(), p));
			}
		});
	}

	protected void startRemoving() {
		view.showMessage("must_take", turn.name(), playerNotInTurn().name());
	}

	protected void updateMove() {
		moveControl.getFsm().update();
	}

	protected boolean isMoveComplete() {
		return moveControl.getFsm().getState() == MoveState.COMPLETE;
	}

	protected boolean isMillClosedByMove() {
		if (isMoveComplete()) {
			Move move = moveControl.getMove().get();
			if (move.isPresent()) {
				return model.board.inMill(move.to().get(), turn.color());
			}
		}
		return false;
	}

	protected void resetGame() {
		view.clearBoard();
		model.whiteStonesPlaced = model.blackStonesPlaced = 0;
		turnPlacingTo(whitePlayer);
	}

	protected boolean isGameOver() {
		return model.board.stoneCount(turn.color()) < 3 || (!turn.canJump() && turn.isTrapped());
	}

	protected void onGameOver() {
		announceWinner(playerNotInTurn());
//		pause(Application.clock.sec(3));
	}

	protected boolean shallStartNewGame() {
		return !whitePlayer.isInteractive() && !blackPlayer.isInteractive()
				|| Keyboard.keyPressedOnce(KeyEvent.VK_SPACE);
	}
	
	public float getPlacingTimeSeconds() {
		return placingTimeSeconds;
	}
}