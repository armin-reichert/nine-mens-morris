package de.amr.games.muehle.view;

import static de.amr.easy.game.math.Vector2f.euclideanDist;
import static de.amr.games.muehle.model.board.Board.NUM_POS;
import static de.amr.games.muehle.model.board.Board.positions;
import static java.lang.Math.round;
import static java.util.stream.Collectors.minBy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.view.View;
import de.amr.games.muehle.model.board.Board;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.model.board.StoneColor;

/**
 * Board user interface.
 * 
 * @author Armin Reichert
 */
public class BoardUI extends Entity implements View {

	static final int[] GRID_X = { 0, 3, 6, 1, 3, 5, 2, 3, 4, 0, 1, 2, 4, 5, 6, 2, 3, 4, 1, 3, 5, 0, 3, 6 };
	static final int[] GRID_Y = { 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6 };

	private final Board board;
	private final Stone[] stones;
	private final Vector2f[] center;

	private int size;
	private int gridSize;
	private int posRadius;
	private Color bgColor;
	private Color lineColor;
	private Font numbersFont;
	private boolean numbersOn;

	public BoardUI(Board board) {
		this.board = board;
		this.stones = new Stone[NUM_POS];
		this.center = new Vector2f[NUM_POS];
	}

	public void setSize(int size) {
		this.size = size;
		tf.setWidth(size);
		tf.setHeight(size);
		this.gridSize = size / 6;
		this.posRadius = size / 60;
		positions().forEach(p -> center[p] = Vector2f.smul(gridSize, Vector2f.of(GRID_X[p], GRID_Y[p])));
		this.numbersFont = new Font("Arial", Font.PLAIN, size / 30);
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public int getStoneRadius() {
		return gridSize / 4;
	}

	public void clear() {
		board.clear();
		Arrays.fill(stones, null);
	}

	public void putStoneAt(int p, StoneColor color) {
		board.putStoneAt(p, color);
		Stone stone = new Stone(color, getStoneRadius());
		stone.tf.setPosition(centerPoint(p));
		stones[p] = stone;
	}

	public Optional<Stone> stoneAt(int p) {
		return Optional.ofNullable(stones[p]);
	}

	public void moveStone(Move move) {
		if (move.isPresent()) {
			int from = move.from().get(), to = move.to().get();
			board.moveStone(from, to);
			Stone stone = stones[from];
			stone.tf.setPosition(centerPoint(to));
			stones[to] = stone;
			stones[from] = null;
		}
	}

	public void removeStoneAt(int p) {
		board.removeStoneAt(p);
		stones[p] = null;
	}

	public Vector2f centerPoint(int p) {
		return center[p];
	}

	private OptionalInt findBoardPosition(float x, float y, int radius) {
		Vector2f point = Vector2f.of(x, y);
		/*@formatter:off*/
		Optional<Integer> opt = positions().boxed()
				.filter(p -> euclideanDist(center[p], point) <= radius)
				.collect(minBy((p1, p2) -> Float.compare(euclideanDist(center[p1], point), euclideanDist(center[p2], point))));
		/*@formatter:on*/
		return opt.isPresent() ? OptionalInt.of(opt.get()) : OptionalInt.empty();
	}

	public OptionalInt findBoardPosition(float x, float y) {
		x -= tf.getX();
		y -= tf.getY();
		if (x < 0) {
			x = 0;
		}
		else if (x > size) {
			x = size;
		}
		if (y < 0) {
			y = 0;
		}
		else if (y > size) {
			y = size;
		}
		return findBoardPosition(x, y, gridSize / 2);
	}

	public void showPositionNumbers() {
		numbersOn = true;
	}

	public void togglePositionNumbers() {
		numbersOn = !numbersOn;
	}

	private void aa_on(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void aa_off(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		// Background
		g.setColor(bgColor);
		g.fillRect(0, 0, size, size);
		// Lines
		g.setColor(lineColor);
		g.setStroke(new BasicStroke(posRadius / 2));
		positions().forEach(from -> Board.neighbors(from).filter(to -> to > from).forEach(to -> {
			g.drawLine(center[from].roundedX(), center[from].roundedY(), center[to].roundedX(),
					center[to].roundedY());
		}));
		// Positions
		positions().forEach(p -> {
			g.setColor(lineColor);
			aa_on(g);
			g.fillOval(center[p].roundedX() - posRadius, center[p].roundedY() - posRadius, 2 * posRadius,
					2 * posRadius);
			aa_off(g);
			if (numbersOn) {
				g.setFont(numbersFont);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.drawString(String.valueOf(p), center[p].x + 2 * posRadius, center[p].y + 4 * posRadius);
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
		});
		// Stones
		Stream.of(stones).filter(Objects::nonNull).forEach(stone -> stone.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markPosition(Graphics2D g, int p, Color color) {
		g.translate(tf.getX(), tf.getY());
		g.setColor(color);
		aa_on(g);
		g.fillOval(round(center[p].x) - posRadius / 2, round(center[p].y) - posRadius / 2, posRadius, posRadius);
		aa_off(g);
		g.translate(-tf.getX(), -tf.getY());
	}

	public void markRemovableStones(Graphics2D g, StoneColor stoneColor) {
		boolean allStonesInMills = board.allStonesInMills(stoneColor);
		board.positions(stoneColor).filter(p -> allStonesInMills || !board.inMill(p, stoneColor)).forEach(p -> {
			stoneAt(p).ifPresent(stone -> {
				float offsetX = tf.getX() + stone.tf.getX() - stone.tf.getWidth() / 2;
				float offsetY = tf.getY() + stone.tf.getY() - stone.tf.getHeight() / 2;
				// draw red cross
				g.translate(offsetX, offsetY);
				g.setColor(Color.RED);
				aa_on(g);
				g.drawLine(0, 0, stone.tf.getWidth(), stone.tf.getHeight());
				g.drawLine(0, stone.tf.getHeight(), stone.tf.getWidth(), 0);
				aa_off(g);
				g.translate(-offsetX, -offsetY);
			});
		});
	}
}