package de.dakror.spamwars.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.InetAddress;

import de.dakror.gamesetup.GameFrame;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.entity.Player;
import de.dakror.spamwars.game.world.World;
import de.dakror.spamwars.layer.LoginLayer;
import de.dakror.spamwars.layer.MenuLayer;
import de.dakror.spamwars.net.Client;
import de.dakror.spamwars.net.PlayerDataSender;
import de.dakror.spamwars.net.Server;
import de.dakror.spamwars.net.User;

/**
 * @author Dakror
 */
public class Game extends GameFrame implements WindowFocusListener
{
	public static World world;
	public static Game currentGame;
	public static Client client;
	public static Server server;
	public static InetAddress ip;
	public static User user;
	public static Player player;
	public static PlayerDataSender playerDataSender;
	
	public Game()
	{
		super();
		currentGame = this;
	}
	
	@Override
	public void draw(Graphics2D g)
	{
		if (world != null) world.draw(g);
		
		drawLayers(g);
		
		Color old = g.getColor();
		g.setColor(Color.green);
		g.setFont(new Font("Arial", Font.PLAIN, 18));
		Helper.drawString(getFPS() + " FPS", 0, 18, g, 18);
		Helper.drawString(getUPS() + " UPS", 100, 18, g, 18);
		
		g.setColor(old);
	}
	
	@Override
	public void initGame()
	{
		w.addWindowFocusListener(this);
		try
		{
			w.setFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/Telev2.ttf")));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		w.setBackground(Color.decode("#D0F4F7"));
		
		client = new Client();
		
		addLayer(new LoginLayer());
		addLayer(new MenuLayer());
		
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		super.keyPressed(e);
		if (world != null) world.keyPressed(e);
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		super.keyReleased(e);
		if (world != null) world.keyReleased(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		super.mouseMoved(e);
		if (world != null) world.mouseMoved(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);
		if (world != null) world.mousePressed(e);
	}
	
	@Override
	public void windowGainedFocus(WindowEvent e)
	{}
	
	@Override
	public void windowLostFocus(WindowEvent e)
	{
		if (player != null)
		{
			player.left = false;
			player.right = false;
			player.up = false;
			player.down = false;
		}
	}
}
