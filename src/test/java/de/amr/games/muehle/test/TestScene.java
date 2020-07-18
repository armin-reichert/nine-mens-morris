package de.amr.games.muehle.test;

import static de.amr.games.muehle.model.board.Board.positions;
import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;
import static java.util.stream.Collectors.joining;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.view.View;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.view.BoardUI;
import de.amr.games.muehle.view.MillGameUI;

public class TestScene implements Lifecycle, View {

	private final MillTestApp app;
	private final Board board;
	private BoardUI boardUI;
	private Color bgColor;

	public TestScene(MillTestApp app) {
		this.app = app;
		bgColor = MillGameUI.BOARD_COLOR;
		board = new Board();
	}

	public int getWidth() {
		return app.settings().width;
	}

	public int getHeight() {
		return app.settings().height;
	}

	@Override
	public void init() {
		boardUI = new BoardUI(board);
		boardUI.setSize(600);
		boardUI.setBgColor(MillGameUI.BOARD_COLOR);
		boardUI.setLineColor(MillGameUI.LINE_COLOR);
		boardUI.showPositionNumbers();
		boardUI.tf.centerBoth(0, 0, getWidth(), getHeight());
	}

	@Override
	public void update() {
		if (Mouse.clicked()) {
			handleMouseClick();
			printBoardInfo();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		boardUI.draw(g);
	}

	private void handleMouseClick() {
		boardUI.findBoardPosition(Mouse.getX(), Mouse.getY()).ifPresent(pos -> {
			if (board.isEmptyPosition(pos)) {
				boardUI.putStoneAt(pos, Mouse.isLeftButton() ? WHITE : BLACK);
			} else {
				boardUI.removeStoneAt(pos);
			}
		});
	}

	private void printBoardInfo() {
		Application.LOGGER.info("Positions opening two white mills: " + toCSV(board.positionsOpeningTwoMills(WHITE)));
		Application.LOGGER.info("Positions opening two black mills: " + toCSV(board.positionsOpeningTwoMills(BLACK)));
		Application.LOGGER.info("Positions opening one white mill: " + toCSV(board.positionsOpeningMill(WHITE)));
		Application.LOGGER.info("Positions opening one black mill: " + toCSV(board.positionsOpeningMill(BLACK)));
		Application.LOGGER.info("Positions closing white mill: " + toCSV(board.positionsClosingMill(WHITE)));
		Application.LOGGER.info("Positions closing black mill: " + toCSV(board.positionsClosingMill(BLACK)));
		Application.LOGGER.info("Positions from where can close white mill: "
				+ toCSV(positions().filter(p -> board.canCloseMillMovingFrom(p, WHITE))));
		Application.LOGGER.info("Positions from where can close black mill: "
				+ toCSV(positions().filter(p -> board.canCloseMillMovingFrom(p, BLACK))));
	}

	private String toCSV(IntStream stream) {
		return stream.mapToObj(String::valueOf).collect(joining(", "));
	}

}