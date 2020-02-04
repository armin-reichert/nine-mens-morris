package de.amr.games.muehle.test;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.muehle.msg.Messages;

/**
 * Mühlespiel aka "Nine men's morris".
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillTestApp extends Application {

	public static void main(String[] args) {
		Locale locale = (args.length > 0) ? new Locale(args[0]) : Locale.getDefault();
		Messages.load(locale);
		launch(MillTestApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.title = "Mill Test Application";
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
	}

	@Override
	public void init() {
		setController(new TestScene(this));
		clock().setTargetFramerate(25);
	}
}