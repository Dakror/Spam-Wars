package de.dakror.spamwars;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.dakror.dakrorbin.DakrorBin;
import de.dakror.dakrorbin.Launch;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.UpdateThread;
import de.dakror.spamwars.game.weapon.Part;
import de.dakror.spamwars.layer.MenuLayer;
import de.dakror.spamwars.net.User;
import de.dakror.spamwars.settings.CFG;

/**
 * @author Dakror
 */
public class SpamWars
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// if (args.length > 0)
		// {
		// if (args[0].equals("test"))
		// {
		// Launch.username = "test";
		// Launch.pwdMd5 = "81dc9bdb52d04dc20036dbd8313ed055";
		// }
		// else if (args[0].equals("test2"))
		// {
		// Launch.username = "test2";
		// Launch.pwdMd5 = "81b073de9370ea873f548e31b8adc081";
		// }
		// }
		// else
		Launch.init(args);
		CFG.INTERNET = Helper.isInternetReachable();
		
		CFG.init();
		
		if (!CFG.INTERNET)
		{
			JOptionPane.showMessageDialog(null, "Um Spam Wars spielen zu können, benötigst du eine aktive Internetverbindung.", "Internetverbindung nicht vorhanden", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		CFG.loadSettings();
		Part.init();
		
		new Game();
		Game.user = new User(Launch.username, null, 0);
		
		Game.currentFrame.init("Spam Wars");
		try
		{
			DakrorBin.init(Game.w, "SpamWars");
			Game.currentFrame.setFullscreen();
		}
		catch (IllegalStateException e)
		{
			System.exit(0);
		}
		Game.currentGame.addLayer(new MenuLayer());
		
		Game.currentFrame.updater = new UpdateThread();
		
		while (true)
			Game.currentFrame.main();
	}
}
