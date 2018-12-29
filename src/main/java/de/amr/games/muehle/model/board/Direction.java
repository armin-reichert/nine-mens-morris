package de.amr.games.muehle.model.board;

public enum Direction {
	NORTH, EAST, SOUTH, WEST;

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	private static final Direction[] OPPOSITE = { SOUTH, WEST, NORTH, EAST };
}