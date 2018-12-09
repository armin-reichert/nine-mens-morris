package de.amr.games.muehle.msg;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

	private static ResourceBundle bundle;

	public static void load(Locale locale) {
		bundle = ResourceBundle.getBundle("de.amr.games.muehle.msg.Messages", locale);
	}

	public static String text(String key, Object... args) {
		try {
			return MessageFormat.format(bundle.getString(key), args);
		} catch (MissingResourceException e) {
			return "MISSING RESOURCE: " + key;
		}
	}
}
