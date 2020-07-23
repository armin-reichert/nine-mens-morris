package de.amr.games.muehle;

import java.util.Locale;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.muehle.controller.game.MillGameController;
import de.amr.games.muehle.model.MillGameModel;
import de.amr.games.muehle.msg.Messages;

/**
 * Mühle (engl. "Nine men's morris").
 * 
 * @author Armin Reichert, Peter und Anna Schillo
 */
public class MillGameApp extends Application {

	public static void main(String... args) {
		Messages.load(args.length > 0 ? new Locale(args[0]) : Locale.getDefault());
		launch(MillGameApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.title = Messages.text("title");
		settings.width = 800;
		settings.height = 800;
		settings.fullScreenMode = null;
	}

	@Override
	public void init() {
		MillGameModel model = new MillGameModel();
		MillGameController controller = new MillGameController(this, model);
		controller.setMoveTimeSeconds(0.75f);
		controller.setPlacingTimeSeconds(1.5f);
		setController(controller);
		clock().setTargetFrameRate(25);
	}
}