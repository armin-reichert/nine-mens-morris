package de.amr.games.muehle.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.widgets.TextWidget;
import de.amr.games.muehle.MillGameApp;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.controller.game.MillGameState;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;
import de.amr.games.muehle.msg.Messages;

/**
 * The scene (UI) of the mill game.
 * 
 * @author Armin Reichert
 */
public class MillGameScene extends Entity implements Lifecycle, MillGameUI {

	private final MillGameApp app;
	private final MillGameController controller;
	private final MillGameModel model;

	private final Color bgColor;
	private BoardUI boardUI;
	private TextWidget messageArea;
	private Stone stoneTemplate;
	private Font stonesCounterFont;

	public MillGameScene(MillGameApp app, MillGameController control) {
		this.app = app;
		this.controller = control;
		this.model = control.model;
		this.bgColor = BOARD_COLOR.darker();
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void update() {

	}

	@Override
	public void init() {
		boardUI = new BoardUI(model.board);
		boardUI.setSize(getWidth() * 3 / 4);
		boardUI.setBgColor(BOARD_COLOR);
		boardUI.setLineColor(LINE_COLOR);
		boardUI.tf.centerX(getWidth());
		boardUI.tf.setY(50);

		stoneTemplate = new Stone(StoneColor.WHITE, boardUI.getStoneRadius());
		stonesCounterFont = new Font(Font.MONOSPACED, Font.BOLD, 2 * boardUI.getStoneRadius());

		messageArea = TextWidget.create().color(Color.BLUE)
				.font(Assets.storeTrueTypeFont("message-font", "fonts/Cookie-Regular.ttf", Font.PLAIN, 36)).build();
		messageArea.tf.setPosition(0, getHeight() - 90);

		controller.assistant.tf.centerX(getWidth());
		controller.assistant.tf.setY(getHeight() / 2 - 100);
	}

	public Lifecycle getController() {
		return controller;
	}

	@Override
	public void clearBoard() {
		if (boardUI != null) {
			boardUI.clear();
		}
	}

	@Override
	public Optional<Stone> getStoneAt(int p) {
		return boardUI.stoneAt(p);
	}

	@Override
	public OptionalInt findBoardPosition(int x, int y) {
		return boardUI.findBoardPosition(x, y);
	}

	@Override
	public void removeStoneAt(int p) {
		boardUI.removeStoneAt(p);
	}

	@Override
	public void putStoneAt(int p, StoneColor color) {
		boardUI.putStoneAt(p, color);
	}

	@Override
	public void moveStone(Move move) {
		boardUI.moveStone(move);
	}

	@Override
	public void showMessage(String key, Object... args) {
		messageArea.setText(Messages.text(key, args));
	}

	@Override
	public Vector2f getLocation(int p) {
		return boardUI.centerPoint(p);
	}

	@Override
	public void markPosition(Graphics2D g, int p, Color color) {
		boardUI.markPosition(g, p, color);
	}

	@Override
	public void markPositions(Graphics2D g, IntStream positions, Color color) {
		positions.forEach(p -> markPosition(g, p, color));
	}

	@Override
	public void toggleBoardPositionNumbers() {
		boardUI.togglePositionNumbers();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
		controller.assistant.draw(g);
		messageArea.tf.centerX(getWidth());
		messageArea.draw(g);
		MillGameState state = controller.getFsm().getState();
		if (state == MillGameState.PLACING || state == MillGameState.PLACING_REMOVING) {
			controller.getPositionNearMouse().ifPresent(p -> {
				if (model.board.isEmptyPosition(p)) {
					boardUI.markPosition(g, p, Color.ORANGE);
				}
			});
			drawStonesLeft(g, controller.whitePlayer(), 9 - model.whiteStonesPlaced, 40, getHeight() - 30);
			drawStonesLeft(g, controller.blackPlayer(), 9 - model.blackStonesPlaced, getWidth() - 100, getHeight() - 30);
		}
		if ((state == MillGameState.MOVING_REMOVING || state == MillGameState.PLACING_REMOVING)
				&& controller.playerInTurn().isInteractive()) {
			boardUI.markRemovableStones(g, controller.playerNotInTurn().color());
		}
	}

	private void drawStonesLeft(Graphics2D g, Player player, int stonesLeft, int x, int y) {
		stoneTemplate.setColor(player.color());
		stoneTemplate.setRadius(boardUI.getStoneRadius() - stonesLeft);
		final int inset = 6;
		g.translate(x + inset * stonesLeft, y - inset * stonesLeft);
		IntStream.range(0, stonesLeft).forEach(i -> {
			stoneTemplate.setRadius(stoneTemplate.getRadius() + 1);
			stoneTemplate.draw(g);
			g.translate(-inset, inset);
		});
		if (stonesLeft > 1) {
			g.setColor(player == controller.playerInTurn() ? Color.RED : Color.DARK_GRAY);
			g.setFont(stonesCounterFont);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(String.valueOf(stonesLeft), 2 * stoneTemplate.getRadius(), stoneTemplate.getRadius());
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		g.translate(-x, -y);
	}
}