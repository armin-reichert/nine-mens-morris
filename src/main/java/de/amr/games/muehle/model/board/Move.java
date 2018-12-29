package de.amr.games.muehle.model.board;

import static de.amr.games.muehle.model.board.Board.checkPosition;

import java.util.Optional;

/**
 * A move or jump.
 * 
 * @author Armin Reichert
 */
public class Move {

	private Integer from;
	private Integer to;

	public Move() {
	}

	public Move(int from, int to) {
		setFrom(from);
		setTo(to);
	}

	public void clear() {
		from = to = null;
	}

	public Optional<Integer> from() {
		return Optional.ofNullable(from);
	}

	public Optional<Integer> to() {
		return Optional.ofNullable(to);
	}

	public void clearTo() {
		to = null;
	}

	public void setFrom(int from) {
		checkPosition(from);
		this.from = from;
	}

	public void setTo(int to) {
		checkPosition(to);
		this.to = to;
	}

	public boolean isPresent() {
		return from != null && to != null;
	}

	@Override
	public String toString() {
		return String.format("Move(%s -> %s)", String.valueOf(from), String.valueOf(to));
	}
}