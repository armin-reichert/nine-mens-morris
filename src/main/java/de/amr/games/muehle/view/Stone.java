package de.amr.games.muehle.view;

import static de.amr.games.muehle.model.board.StoneColor.BLACK;
import static de.amr.games.muehle.model.board.StoneColor.WHITE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;
import de.amr.games.muehle.model.board.StoneColor;

/**
 * Visual representation of a stone.
 * 
 * @author Armin Reichert
 */
public class Stone extends Entity implements View {

	static final EnumMap<StoneColor, Color> FILL = new EnumMap<>(StoneColor.class);
	static final EnumMap<StoneColor, Color> EDGE = new EnumMap<>(StoneColor.class);

	static {
		FILL.put(WHITE, new Color(255, 248, 220));
		FILL.put(BLACK, Color.BLACK);
		EDGE.put(WHITE, new Color(255, 248, 220).darker());
		EDGE.put(BLACK, Color.DARK_GRAY);
	}

	private StoneColor color;
	private int radius;

	public Stone(StoneColor color, int radius) {
		this.color = color;
		this.radius = radius;
		tf.width =(2 * radius);
		tf.height =(2 * radius);
	}

	public void setColor(StoneColor color) {
		this.color = color;
	}

	public StoneColor getColor() {
		return color;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	@Override
	public void draw(Graphics2D g) {
		int size = tf.height;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(tf.x - radius, tf.y - radius);
		g.setColor(FILL.get(color));
		g.fillOval(0, 0, size, size);
		g.setColor(EDGE.get(color));
		g.setStroke(new BasicStroke(2));
		Stream.of(1f, 0.8f, 0.4f).map(p -> p * size).forEach(dia -> {
			int offset = (int) ((size - dia) / 2f);
			g.drawOval(offset, offset, dia.intValue(), dia.intValue());
		});
		g.translate(-tf.x + radius, -tf.y + radius);
	}
}