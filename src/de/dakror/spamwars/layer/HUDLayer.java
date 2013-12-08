package de.dakror.spamwars.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import de.dakror.gamesetup.ui.Component;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.net.User;
import de.dakror.spamwars.net.packet.Packet;
import de.dakror.spamwars.net.packet.Packet04PlayerList;
import de.dakror.spamwars.net.packet.Packet09Kill;
import de.dakror.spamwars.ui.KillLabel;

/**
 * @author Dakror
 */
public class HUDLayer extends MPLayer
{
	public static Comparator<User> sort = new Comparator<User>()
	{
		@Override
		public int compare(User o1, User o2)
		{
			int K1 = o1.K, K2 = o2.K, D1 = o1.D, D2 = o2.D;
			if (K1 == 0 && K2 == 0) return Integer.compare(D1, D2);
			
			if (D1 == 0) D1++;
			if (D2 == 0) D2++;
			
			return Float.compare(K2 / (float) D2, K1 / (float) D1);
		}
	};
	
	boolean showStats;
	public boolean reload;
	public int reloadStarted;
	int tick;
	int killY;
	
	BufferedImage stats;
	boolean invokeRenderStats = false;
	
	@Override
	public void draw(Graphics2D g)
	{
		Helper.drawContainer(Game.getWidth() / 2 - 200, Game.getHeight() - 50, 400, 60, false, false, g);
		Helper.drawProgressBar(Game.getWidth() / 2 - 180, Game.getHeight() - 30, 360, Game.player.getLife() / (float) Game.player.getMaxlife(), "ff3232", g);
		Color o = g.getColor();
		g.setColor(Color.black);
		Helper.drawHorizontallyCenteredString(Game.player.getLife() + " / " + Game.player.getMaxlife(), Game.getWidth(), Game.getHeight() - 14, g, 14);
		
		Helper.drawContainer(Game.getWidth() - 175, Game.getHeight() - 110, 175, 110, false, false, g);
		g.setColor(Color.white);
		Helper.drawString(Game.player.getWeapon().ammo + "", Game.getWidth() - 165, Game.getHeight() - 50, g, 70);
		Helper.drawRightAlignedString(Game.player.getWeapon().capacity + "", Game.getWidth() - 10, Game.getHeight() - 15, g, 40);
		
		// -- time panel -- //
		Helper.drawContainer(Game.getWidth() / 2 - 150, 0, 300, 80, true, true, g);
		Helper.drawHorizontallyCenteredString(Game.client.isGameOver() ? "00:00" : new SimpleDateFormat("mm:ss").format(new Date((Game.client.gameStarted + Game.client.gameInfo.getMinutes() * 60000) - System.currentTimeMillis())), Game.getWidth(), 56, g, 50);
		
		
		if (!new Rectangle(5, 5, 70, 70).contains(Game.currentGame.mouse) || !Game.currentGame.getActiveLayer().equals(this)) Helper.drawContainer(5, 5, 70, 70, false, false, g);
		else Helper.drawContainer(0, 0, 80, 80, false, true, g);
		g.drawImage(Game.getImage("gui/pause.png"), 5, 5, 70, 70, Game.w);
		
		if (reload && reloadStarted > 0)
		{
			Composite c = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
			Helper.drawShadow(Game.getWidth() / 2 - 260, Game.getHeight() / 3 * 2 - 10, 520, 40, g);
			Helper.drawOutline(Game.getWidth() / 2 - 260, Game.getHeight() / 3 * 2 - 10, 520, 40, false, g);
			Helper.drawProgressBar(Game.getWidth() / 2 - 251, Game.getHeight() / 3 * 2 - 1, 500, (tick - reloadStarted) / (float) Game.player.getWeapon().reloadSpeed, "2a86e7", g);
			g.setColor(Color.black);
			Helper.drawHorizontallyCenteredString("Nachladen", Game.getWidth(), Game.getHeight() / 3 * 2 + 16, g, 20);
			g.setComposite(c);
		}
		
		g.setColor(o);
		
		drawComponents(g);
		
		drawStats(g);
	}
	
	public void drawStats(Graphics2D g)
	{
		if (stats == null || invokeRenderStats)
		{
			stats = new BufferedImage(Game.getWidth(), Game.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g1 = (Graphics2D) stats.getGraphics();
			g1.setRenderingHints(g.getRenderingHints());
			g1.setFont(g.getFont());
			renderStats(g1);
			invokeRenderStats = false;
		}
		
		if (!showStats) return;
		g.drawImage(stats, 0, 0, Game.w);
	}
	
	public void renderStats(Graphics2D g)
	{
		Helper.drawContainer(Game.getWidth() / 2 - 500, Game.getHeight() / 2 - 300, 1000, 600, true, false, g);
		Color o = g.getColor();
		g.setColor(Color.gray);
		Helper.drawHorizontallyCenteredString("Statistik", Game.getWidth(), Game.getHeight() / 2 - 220, g, 80);
		Helper.drawOutline(Game.getWidth() / 2 - 495, Game.getHeight() / 2 - 295, 990, 100, false, g);
		User[] users = Game.client.serverInfo.getUsers();
		Arrays.sort(users, sort);
		Helper.drawString("SPIELERNAME", Game.getWidth() / 2 - 450, Game.getHeight() / 2 - 160, g, 30);
		Helper.drawString("K / D", Game.getWidth() / 2 + 300, Game.getHeight() / 2 - 160, g, 30);
		
		for (int i = 0; i < users.length; i++)
		{
			g.setColor(Color.white);
			if (users[i].getUsername().equals(Game.user.getUsername())) g.setColor(Color.decode("#3333ff"));
			Helper.drawString(users[i].getUsername(), Game.getWidth() / 2 - 450, Game.getHeight() / 2 - 110 + i * 30, g, 30);
			Helper.drawString("/", Game.getWidth() / 2 + 350, Game.getHeight() / 2 - 110 + i * 30, g, 30);
			Helper.drawRightAlignedString(users[i].K + "", Game.getWidth() / 2 + 331, Game.getHeight() / 2 - 110 + i * 30, g, 30);
			Helper.drawString(users[i].D + "", Game.getWidth() / 2 + 391, Game.getHeight() / 2 - 110 + i * 30, g, 30);
		}
		g.setColor(o);
	}
	
	@Override
	public void update(int tick)
	{
		this.tick = tick;
		
		if (reload)
		{
			if (reloadStarted == 0) reloadStarted = tick;
			
			if (tick - reloadStarted >= Game.player.getWeapon().reloadSpeed)
			{
				Game.player.getWeapon().reload();
				reload = false;
			}
		}
		
		Game.player.getWeapon().reloading = reload;
		
		killY = 30;
		
		int killed = 0;
		
		for (Component c : components)
		{
			if (c instanceof KillLabel)
			{
				if (((KillLabel) c).dead)
				{
					killed++;
					components.remove(c);
				}
				else
				{
					c.y -= killed * KillLabel.SPACE;
					
					if (c.y + KillLabel.SPACE > killY) killY = c.y + KillLabel.SPACE;
				}
			}
		}
		
		if (Game.currentGame.getActiveLayer().equals(this) && Game.client.isGameOver())
		{
			Game.currentGame.addLayer(new WinnerLayer());
		}
		
		updateComponents(tick);
	}
	
	@Override
	public void onPacketReceived(Packet p)
	{
		if (p instanceof Packet09Kill) components.add(new KillLabel(killY, ((Packet09Kill) p).getKiller(), ((Packet09Kill) p).getDead(), ((Packet09Kill) p).getWeapon()));
		if (p instanceof Packet04PlayerList) invokeRenderStats = true;
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		super.keyPressed(e);
		
		if (e.getKeyCode() == KeyEvent.VK_TAB) showStats = true;
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) Game.currentGame.addLayer(new PauseLayer());
		if (e.getKeyCode() == KeyEvent.VK_R && Game.player.getWeapon().canReload() && !reload)
		{
			reload = true;
			reloadStarted = 0;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		super.keyReleased(e);
		
		if (e.getKeyCode() == KeyEvent.VK_TAB) showStats = false;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		
		if (new Rectangle(5, 5, 70, 70).contains(e.getPoint())) Game.currentGame.addLayer(new PauseLayer());
	}
	
	@Override
	public void init()
	{
		showStats = false;
	}
}
