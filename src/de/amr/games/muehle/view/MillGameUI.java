package de.amr.games.muehle.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.View;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;

public interface MillGameUI extends View {

	static final Color BOARD_COLOR = new Color(255, 255, 224);
	static final Color LINE_COLOR = Color.BLACK;

	void clearBoard();

	Optional<Stone> getStoneAt(int p);

	OptionalInt findBoardPosition(int x, int y);

	void removeStoneAt(int p);

	void putStoneAt(int p, StoneColor color);

	void moveStone(Move move);

	void showMessage(String key, Object... args);

	Vector2f getLocation(int p);

	void markPosition(Graphics2D g, int p, Color color);

	void markPositions(Graphics2D g, IntStream positions, Color color);

	void toggleBoardPositionNumbers();
}