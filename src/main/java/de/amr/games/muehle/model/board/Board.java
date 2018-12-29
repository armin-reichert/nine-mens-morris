package de.amr.games.muehle.model.board;

import static de.amr.games.muehle.model.board.Direction.NORTH;
import static de.amr.games.muehle.model.board.Direction.SOUTH;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents the board as an undirected graph and provides information about the board content and
 * mill-related functionality.
 *
 * @author Armin Reichert, Peter & Anna Schillo
 */
public class Board {

	/** The number of positions (nodes). */
	public static final int NUM_POS = 24;

	/*
	 * An adjacency list-like representation of the board graph.
	 * 
	 * NEIGHBOR[p] = { neighbor(p, NORTH), neighbor(p, EAST), neighbor(p, SOUTH), neighbor(p, WEST) },
	 * -1 = no neighbor
	 */
	private static final int[][] NEIGHBOR = {
		/*@formatter:off*/
		{ -1, 1, 9, -1 }, 
		{ -1, 2, 4, 0 }, 
		{ -1, -1, 14, 1 }, 
		{ -1, 4, 10, -1 },
		{ 1, 5, 7, 3 }, 
		{ -1, -1, 13, 4 },
		{ -1, 7, 11, -1 }, 
		{ 4, 8, -1, 6 }, 
		{ -1, -1, 12, 7 },
		{ 0, 10, 21, -1 }, 
		{ 3, 11, 18, 9 },
		{ 6, -1, 15, 10 }, 
		{ 8, 13, 17, -1 }, 
		{ 5, 14, 20, 12 }, 
		{ 2, -1, 23, 13 },
		{ 11, 16, -1, -1 },
		{ -1, 17, 19, 15 }, 
		{ 12, -1, -1, 16 }, 
		{ 10, 19, -1, -1 },
		{ 16, 20, 22, 18 }, 
		{ 13, -1, -1, 19 },
		{ 9, 22, -1, -1 }, 
		{ 19, 23, -1, 21 }, 
		{ 14, -1, -1, 22 },
		/*@formatter:on*/
	};

	/*
	 * Horizontal neighbor positions.
	 * 
	 * The set { p, ROW[p][0], ROW[p][1] } forms a row.
	 */
	private static final int[][] ROW = {
			/*@formatter:off*/
			{ 1, 2 },	
			{ 0, 2 },
			{ 0, 1 },
			{ 4, 5 },
			{ 3, 5 },
			{ 3, 4 },
			{ 7, 8 },
			{ 6, 8 },
			{ 6, 7 },
			{ 10, 11 },
			{ 9, 11 },
			{ 9, 10 },
			{ 13, 14 },
			{ 12, 14 },
			{ 12, 13 },
			{ 16, 17 }, 
			{ 15, 17 },
			{ 15, 16 },
			{ 19, 20 },
			{ 18, 20 },
			{ 18, 19 },
			{ 22, 23 },
			{ 21, 23 },
			{ 21, 22 }
			/*@formatter:on*/
	};

	/*
	 * Vertical neighbor positions.
	 * 
	 * The set { p, COL[p][0], COL[p][1] } forms a column.
	 */
	private static final int[][] COL = {
			/*@formatter:off*/
			{ 9, 21 },	
			{ 4, 7 },
			{ 14, 23 },
			{ 10, 18 },
			{ 1, 7 },
			{ 13, 20 },
			{ 11, 15 },
			{ 1, 4 },
			{ 12, 17 },
			{ 0, 21 },
			{ 3, 18 },
			{ 6, 15 },
			{ 8, 17 },
			{ 5, 20 },
			{ 2, 23 },
			{ 6, 11 }, 
			{ 19, 22 },
			{ 8, 12 },
			{ 3, 10 },
			{ 16, 22 },
			{ 5, 13 },
			{ 0, 9 },
			{ 16, 19 },
			{ 2, 14 }
			/*@formatter:on*/
	};

	public static void checkPosition(int p) {
		if (!isValidPosition(p)) {
			throw new IllegalArgumentException("Illegal position: " + p);
		}
	}

	public static void checkDirection(Direction dir) {
		if (dir == null) {
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	public static void checkStoneColor(StoneColor color) {
		if (color == null) {
			throw new IllegalArgumentException("Illegal stone color: " + color);
		}
	}

	/**
	 * @param p
	 *            some number
	 * @return if the given number denotes a valid board position
	 */
	public static boolean isValidPosition(int p) {
		return 0 <= p && p < NUM_POS;
	}

	/**
	 * @return a stream of the board positions
	 */
	public static IntStream positions() {
		return IntStream.range(0, NUM_POS);
	}

	/**
	 * @param p
	 *            a position
	 * @return a stream of the neighbor positions
	 */
	public static IntStream neighbors(int p) {
		checkPosition(p);
		return IntStream.of(NEIGHBOR[p]).filter(q -> q != -1);
	}

	/**
	 * @param p
	 *              a position
	 * @param dir
	 *              a direction
	 * @return the (optional) neighbor in the given direction
	 */
	public static OptionalInt neighbor(int p, Direction dir) {
		checkPosition(p);
		checkDirection(dir);
		int q = NEIGHBOR[p][dir.ordinal()];
		return q != -1 ? OptionalInt.of(q) : OptionalInt.empty();
	}

	/**
	 * @param p
	 *            a position
	 * @return stream of all positions which have distance 2 from given position
	 */
	public static IntStream nextToNeighbors(int p) {
		checkPosition(p);
		return neighbors(p).flatMap(Board::neighbors).distinct().filter(q -> q != p);
	}

	/**
	 * @param p
	 *            a position
	 * @param q
	 *            a position
	 * @return if the given positions are neighbors
	 */
	public static boolean areNeighbors(int p, int q) {
		checkPosition(p);
		checkPosition(q);
		return neighbors(p).anyMatch(n -> n == q);
	}

	/**
	 * @param p
	 *            a position
	 * @param q
	 *            a position
	 * @return the (optional) direction from <code>p</code> to <code>q</code> if <code>p</code> and
	 *         <code>q</code> are neighbors
	 */
	public static Optional<Direction> getDirection(int p, int q) {
		checkPosition(p);
		checkPosition(q);
		return Stream.of(Direction.values()).filter(dir -> NEIGHBOR[p][dir.ordinal()] == q).findFirst();
	}

	/**
	 * @param p
	 *            a position
	 * @param q
	 *            a position
	 * @param r
	 *            a position
	 * @return if the given positions form a row
	 */
	public static boolean inRow(int p, int q, int r) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		int h1 = ROW[p][0], h2 = ROW[p][1];
		return h1 == q && h2 == r || h1 == r && h2 == q;
	}

	/**
	 * @param p
	 *            a position
	 * @param q
	 *            a position
	 * @param r
	 *            a position
	 * @return if the given positions form a column
	 */
	public static boolean inCol(int p, int q, int r) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		int v1 = COL[p][0], v2 = COL[p][1];
		return v1 == q && v2 == r || v1 == r && v2 == q;
	}

	// non-static content

	private final StoneColor[] content;

	private void set(int p, StoneColor color) {
		content[p] = color;
	}

	private StoneColor get(int p) {
		return content[p];
	}

	private boolean has(int p, StoneColor color) {
		return content[p] == color;
	}

	/**
	 * Constructs an empty board.
	 */
	public Board() {
		content = new StoneColor[NUM_POS];
	}

	/**
	 * Clears the board.
	 */
	public void clear() {
		Arrays.fill(content, null);
	}

	/**
	 * @param color
	 *                a stone color
	 * @return a stream of the positions with a stone of the given color
	 */
	public IntStream positions(StoneColor color) {
		checkStoneColor(color);
		return positions().filter(p -> has(p, color));
	}

	/**
	 * @return the number of stones
	 */
	public long stoneCount() {
		return Stream.of(content).filter(Objects::nonNull).count();
	}

	/**
	 * @param color
	 *                a stone color
	 * @return the number of stones with the given color
	 */
	public long stoneCount(StoneColor color) {
		checkStoneColor(color);
		return Stream.of(content).filter(c -> c == color).count();
	}

	/**
	 * Puts a stone with the given color at the given position. The position must not contain a stone
	 * already.
	 * 
	 * @param p
	 *                a position
	 * @param color
	 *                a stone color
	 */
	public void putStoneAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		if (!has(p, null)) {
			throw new IllegalStateException("Position where stone is placed must be empty");
		}
		set(p, color);
	}

	/**
	 * Removes a stone from the given position. Empty positions are allowed.
	 * 
	 * @param p
	 *            a valid position
	 */
	public void removeStoneAt(int p) {
		checkPosition(p);
		set(p, null);
	}

	/**
	 * Moves the stone from position <code>from</code> to position <code>to</code>. If the source
	 * position is empty or the target position is not empty, an exception is thrown.
	 * 
	 * @param from
	 *               the source position
	 * @param to
	 *               the target position
	 */
	public void moveStone(int from, int to) {
		checkPosition(from);
		checkPosition(to);
		if (has(from, null)) {
			throw new IllegalStateException("Position from where stone is moved must not be empty");
		}
		if (!has(to, null)) {
			throw new IllegalStateException("Position where stone is moved to must be empty");
		}
		set(to, get(from));
		set(from, null);
	}

	/**
	 * @param p
	 *            a position
	 * @return the (optional) content at this position
	 */
	public Optional<StoneColor> getStoneAt(int p) {
		checkPosition(p);
		return Optional.ofNullable(get(p));
	}

	/**
	 * @param p
	 *            a valid position
	 * @return if the position is empty
	 */
	public boolean isEmptyPosition(int p) {
		checkPosition(p);
		return has(p, null);
	}

	/**
	 * @return stream of all empty positions on the board
	 */
	public IntStream emptyPositions() {
		return positions().filter(p -> has(p, null));
	}

	/**
	 * @param p
	 *            a valid position
	 * @return if there is a stone at the position
	 */
	public boolean hasStoneAt(int p) {
		checkPosition(p);
		return !has(p, null);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if there is a stone of the given color at this position
	 */
	public boolean hasStoneAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color);
	}

	/**
	 * @param p
	 *            a valid position
	 * @return if the position p has an empty neighbor position
	 */
	public boolean hasEmptyNeighbor(int p) {
		checkPosition(p);
		return neighbors(p).anyMatch(this::isEmptyPosition);
	}

	/**
	 * @param p
	 *            a valid position
	 * @return a stream of the empty neighbor positions of p
	 */
	public IntStream emptyNeighbors(int p) {
		checkPosition(p);
		return neighbors(p).filter(this::isEmptyPosition);
	}

	/**
	 * @param color
	 *                a stone color
	 * @return a stream of the positions carrying a stone of the given color and having an empty
	 *         neighbor position
	 */
	public IntStream positionsWithEmptyNeighbor(StoneColor color) {
		return positions(color).filter(this::hasEmptyNeighbor);
	}

	/**
	 * @param color
	 *                a stone color
	 * @return if no stone of the given color can move (jumping not possible)
	 */
	public boolean isTrapped(StoneColor color) {
		checkStoneColor(color);
		return positions(color).noneMatch(this::hasEmptyNeighbor);
	}

	// Mill related methods

	/**
	 * @param p
	 *                a valid position
	 * @param q
	 *                a valid position
	 * @param r
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given positions form a horizontal mill of the given color
	 */
	public boolean hasHMill(int p, int q, int r, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		checkStoneColor(color);
		return inRow(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> has(pos, color));
	}

	/**
	 * @param p
	 *                a valid position
	 * @param q
	 *                a valid position
	 * @param r
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given positions form a vertical mill of the given color
	 */
	public boolean hasVMill(int p, int q, int r, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkPosition(r);
		checkStoneColor(color);
		return inCol(p, q, r) && IntStream.of(p, q, r).allMatch(pos -> has(pos, color));
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is inside a horizontal mill of the given color
	 */
	public boolean inHMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return IntStream.of(p, ROW[p][0], ROW[p][1]).allMatch(q -> has(q, color));
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is inside a vertical mill of the given color
	 */
	public boolean inVMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return IntStream.of(p, COL[p][0], COL[p][1]).allMatch(q -> has(q, color));
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is inside a mill of the given color
	 */
	public boolean inMill(int p, StoneColor color) {
		return inHMill(p, color) || inVMill(p, color);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is part of an open mill of the given color
	 */
	public boolean isPartOfOpenMill(int p, StoneColor color) {
		return isPartOfOpenHMill(p, color) || isPartOfOpenVMill(p, color);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is part of an open horizontal mill of the given color
	 */
	public boolean isPartOfOpenHMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isPartOfOpenXMill(p, color, ROW);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if the given position is part of an open vertical mill of the given color
	 */
	public boolean isPartOfOpenVMill(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isPartOfOpenXMill(p, color, COL);
	}

	private boolean isPartOfOpenXMill(int p, StoneColor color, int[][] mill) {
		int q = mill[p][0], r = mill[p][1];
		return has(p, color) && has(q, color) && has(r, null)
				|| has(p, color) && has(q, null) && has(r, color);
	}

	/**
	 * @param color
	 *                a stone color
	 * @return if all stones of the given color are inside some mill
	 */
	public boolean allStonesInMills(StoneColor color) {
		return positions(color).allMatch(p -> inMill(p, color));
	}

	/**
	 * @param color
	 *                a stone color
	 * @return a stream of all positions where a mill of the given color could be closed
	 */
	public IntStream positionsClosingMill(StoneColor color) {
		return positions().filter(p -> isMillClosingPosition(p, color));
	}

	/**
	 * @param color
	 *                a stone color
	 * @return a stream of all positions where a mill of the given color could be opened
	 */
	public IntStream positionsOpeningMill(StoneColor color) {
		return positions().filter(p -> isMillOpenedAt(p, color));
	}

	/**
	 * @param color
	 *                a stone color
	 * @return a stream of all positions where two mills of the given color could be opened at once
	 */
	public IntStream positionsOpeningTwoMills(StoneColor color) {
		return positions().filter(p -> areTwoMillsOpenedAt(p, color));
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if placing a stone of the given color at the given position opens a horizontal mill
	 */
	public boolean isHMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXMillOpenedAt(p, color, ROW[p]);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if placing a stone of the given color at the given position opens a vertical mill
	 */
	public boolean isVMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isXMillOpenedAt(p, color, COL[p]);
	}

	private boolean isXMillOpenedAt(int p, StoneColor color, int[] mill) {
		int q = mill[0], r = mill[1];
		return has(p, null) && has(q, color) && has(r, null)
				|| has(p, null) && has(q, null) && has(r, color);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if placing a stone of the given color at the given position would open a mill
	 */
	public boolean isMillOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isHMillOpenedAt(p, color) || isVMillOpenedAt(p, color);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if placing a stone of the given color at the given position would open two mills of
	 *         that color
	 */
	public boolean areTwoMillsOpenedAt(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isHMillOpenedAt(p, color) && isVMillOpenedAt(p, color);
	}

	/**
	 * @param p
	 *                a valid position
	 * @param color
	 *                a stone color
	 * @return if a mill of the given color is closed by placing a stone of that color at the given
	 *         position
	 */
	public boolean isMillClosingPosition(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return isHMillClosingPosition(p, color) || isVMillClosingPosition(p, color);
	}

	private boolean isHMillClosingPosition(int p, StoneColor color) {
		return has(p, null) && has(ROW[p][0], color) && has(ROW[p][1], color);
	}

	private boolean isVMillClosingPosition(int p, StoneColor color) {
		return has(p, null) && has(COL[p][0], color) && has(COL[p][1], color);
	}

	/**
	 * @param fromMil
	 *                  move start position
	 * @param to
	 *                  move end position
	 * @param color
	 *                  stone color
	 * @return if a mill of the given color is closed when moving a stone of the given color from the
	 *         start to the end position
	 */
	public boolean isMillClosedByMove(int from, int to, StoneColor color) {
		checkPosition(from);
		checkPosition(to);
		checkStoneColor(color);
		if (has(from, color) && has(to, null)) {
			Optional<Direction> optDir = getDirection(from, to);
			if (optDir.isPresent()) {
				Direction dir = optDir.get();
				if (dir == NORTH || dir == SOUTH) {
					return has(ROW[to][0], color) && has(ROW[to][1], color);
				} else {
					return has(COL[to][0], color) && has(COL[to][1], color);
				}
			}
		}
		return false;
	}

	public boolean isMillClosedByJump(int from, int to, StoneColor color) {
		checkPosition(from);
		checkPosition(to);
		checkStoneColor(color);
		return has(from, color) && isMillClosingPosition(to, color);
	}

	/**
	 * @param p
	 *                valid position
	 * @param color
	 *                stone color
	 * @return if a mill of the given color can be closed when moving from p
	 */
	public boolean canCloseMillMovingFrom(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return has(p, color) && (getVMillClosingNeighbors(p, color).findFirst().isPresent()
				|| getHMillClosingNeighbors(p, color).findFirst().isPresent());
	}

	private IntStream getVMillClosingNeighbors(int p, StoneColor color) {
		return emptyNeighbors(p).filter(n -> isVMillClosingPosition(n, color)).filter(n -> {
			Direction dir = getDirection(n, p).get();
			return dir == Direction.WEST || dir == Direction.EAST;
		});
	}

	private IntStream getHMillClosingNeighbors(int p, StoneColor color) {
		return emptyNeighbors(p).filter(n -> isHMillClosingPosition(n, color)).filter(n -> {
			Direction dir = getDirection(n, p).get();
			return dir == Direction.NORTH || dir == Direction.SOUTH;
		});
	}

	public boolean canCloseMillJumpingFrom(int from, StoneColor color) {
		checkPosition(from);
		checkStoneColor(color);
		return has(from, color) && positionsClosingMill(color).findAny().isPresent();
	}

	/**
	 * @param color
	 *                stone color
	 * @return positions where by placing a stone two mills of the same color could be opened later
	 */
	public IntStream positionsOpeningTwoMillsLater(StoneColor color) {
		checkStoneColor(color);
		return positions().filter(this::isEmptyPosition)
				.filter(p -> hasTwoMillsLaterPartnerPosition(p, color));
	}

	/**
	 * @param p
	 *                valid position
	 * @param color
	 *                stone color
	 * @return if by placing a stone of the given color at the position later two mills could be
	 *         opened
	 */
	public boolean hasTwoMillsLaterPartnerPosition(int p, StoneColor color) {
		checkPosition(p);
		checkStoneColor(color);
		return nextToNeighbors(p).filter(q -> get(q) == color)
				.anyMatch(q -> areTwoMillsPossibleLater(p, q, color));
	}

	private boolean areTwoMillsPossibleLater(int p, int q, StoneColor color) {
		checkPosition(p);
		checkPosition(q);
		checkStoneColor(color);
		int commonNeighbor = neighbors(p).filter(n -> areNeighbors(n, q)).findFirst().getAsInt();
		Direction dir1 = getDirection(p, commonNeighbor).get();
		OptionalInt otherNeighbor1 = neighbor(p, dir1.opposite());
		if (!otherNeighbor1.isPresent()) {
			otherNeighbor1 = neighbor(commonNeighbor, dir1);
		}
		Direction dir2 = getDirection(q, commonNeighbor).get();
		OptionalInt otherNeighbor2 = neighbor(q, dir2.opposite());
		if (!otherNeighbor2.isPresent()) {
			otherNeighbor2 = neighbor(commonNeighbor, dir2);
		}
		return otherNeighbor1.isPresent() && isEmptyPosition(otherNeighbor1.getAsInt())
				&& otherNeighbor2.isPresent() && isEmptyPosition(otherNeighbor2.getAsInt());
	}
}