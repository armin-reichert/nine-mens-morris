package de.amr.games.muehle.model;

import de.amr.games.muehle.model.board.Board;

/**
 * Data model of the mill game.
 * 
 * @author Armin Reichert
 */
public class MillGameModel {

	public final Board board;
	public int whiteStonesPlaced;
	public int blackStonesPlaced;

	public MillGameModel() {
		this.board = new Board();
	}
}