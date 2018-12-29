package de.amr.games.muehle.util;

import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

public class Util {

	private static final Random RAND = new Random();

	/**
	 * @param stream
	 *                 stream of integers
	 * @return a random value from the stream
	 */
	public static OptionalInt randomElement(IntStream stream) {
		int[] elements = stream.toArray();
		return elements.length == 0 ? OptionalInt.empty()
				: OptionalInt.of(elements[RAND.nextInt(elements.length)]);
	}

}
