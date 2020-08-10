package de.amr.games.muehle.view;

import static de.amr.easy.game.Application.loginfo;
import static de.amr.games.muehle.model.board.Board.neighbors;
import static de.amr.games.muehle.model.board.Board.positions;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.controller.game.MillGameState;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;

/**
 * An assistant providing visual and acoustic hints to the assisted player.
 */
public class Assistant extends Entity implements Lifecycle, View {

	public enum HelpLevel {
		OFF, NORMAL, HIGH
	}

	/** Enumeration type for sound IDs */
	private enum SoundID {
		CAN_CLOSE_MILL("can_close_mill"), CAN_OPPONENT_CLOSE_MILL("can_opponent_close_mill"),
		CAN_OPEN_TWO_MILLS("can_open_two_mills"), YO_FINE("yo_fine"), WIN("win");

		private SoundID(String baseName) {
			assetsPath = "sfx/" + baseName + ".mp3";
		}

		public SoundClip sound() {
			return Assets.sound(assetsPath);
		}

		private final String assetsPath;
	}

	public final Image alien;
	private final Board board;
	private final MillGameController control;
	private MillGameUI view;
	private HelpLevel helpLevel;

	public Assistant(MillGameController control) {
		this.control = control;
		this.board = control.model.board;
		this.helpLevel = HelpLevel.OFF;
		alien = Assets.image("images/alien.png").getScaledInstance(100, 100, BufferedImage.SCALE_DEFAULT);
		tf.width = (alien.getWidth(null));
		tf.height = (alien.getHeight(null));
	}

	public void setView(MillGameUI view) {
		this.view = view;
	}

	public HelpLevel getHelpLevel() {
		return helpLevel;
	}

	public void setHelpLevel(HelpLevel level) {
		helpLevel = level;
		if (helpLevel == HelpLevel.OFF) {
			loginfo(Messages.text("assistant_off"));
		} else {
			tellYoFine();
			loginfo(Messages.text("assistant_on"));
		}
	}

	@Override
	public void init() {
		// preload sounds
		Stream.of(SoundID.values()).forEach(SoundID::sound);
	}

	@Override
	public void update() {
	}

	public void toggle() {
		setHelpLevel(helpLevel == HelpLevel.OFF ? HelpLevel.NORMAL : HelpLevel.OFF);
	}

	@Override
	public void draw(Graphics2D g) {
		// draw assistant only if any sound is running
		if (helpLevel != HelpLevel.OFF && Stream.of(SoundID.values()).map(SoundID::sound).anyMatch(SoundClip::isRunning)) {
			g.drawImage(alien, (int) tf.x, (int) tf.y, null);
			if (helpLevel == HelpLevel.HIGH && control.playerInTurn().isInteractive()) {
				MillGameState state = control.getFsm().getState();
				if (state == MillGameState.PLACING || state == MillGameState.PLACING_REMOVING) {
					view.markPositions(g, board.positionsClosingMill(control.playerInTurn().color()), Color.GREEN);
					view.markPositions(g, board.positionsOpeningTwoMills(control.playerInTurn().color()), Color.YELLOW);
					view.markPositions(g, board.positionsClosingMill(control.playerNotInTurn().color()), Color.RED);
				} else if (state == MillGameState.MOVING || state == MillGameState.MOVING_REMOVING) {
					markPossibleMoveStarts(g, control.playerInTurn().color(), Color.GREEN);
					markTrappingPosition(g, control.playerInTurn().color(), control.playerNotInTurn().color(), Color.RED);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g, StoneColor stoneColor, Color color) {
		(control.playerInTurn().canJump() ? board.positions(stoneColor) : board.positionsWithEmptyNeighbor(stoneColor))
				.forEach(p -> view.markPosition(g, p, color));
	}

	private void markTrappingPosition(Graphics2D g, StoneColor either, StoneColor other, Color color) {
		if (board.positionsWithEmptyNeighbor(other).count() == 1) {
			int singleFreePosition = board.positionsWithEmptyNeighbor(other).findFirst().getAsInt();
			if (neighbors(singleFreePosition).filter(board::hasStoneAt).anyMatch(p -> board.getStoneAt(p).get() == either)) {
				view.markPosition(g, singleFreePosition, color);
			}
		}
	}

	private void play(SoundID soundID) {
		if (helpLevel != HelpLevel.OFF) {
			soundID.sound().play();
		}
	}

	public void givePlacingHint(Player player) {
		if (helpLevel != HelpLevel.OFF) {
			IntStream positions;
			StoneColor color = player.color();

			// can opponent close mill?
			positions = positions().filter(p -> board.isMillClosingPosition(p, color.other()));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_OPPONENT_CLOSE_MILL);
				return;
			}

			// can close own mill?
			positions = positions().filter(p -> board.isMillClosingPosition(p, color));
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_CLOSE_MILL);
				return;
			}

			// can open two mills at once?
			positions = board.positionsOpeningTwoMills(color);
			if (positions.findAny().isPresent()) {
				play(SoundID.CAN_OPEN_TWO_MILLS);
				return;
			}
		}
	}

	public void tellMillClosed() {
		play(SoundID.YO_FINE);
	}

	public void tellYoFine() {
		play(SoundID.YO_FINE);
	}

	public void tellWin(Player player) {
		play(SoundID.WIN);
	}
}