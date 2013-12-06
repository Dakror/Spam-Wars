package de.dakror.spamwars.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.dakror.gamesetup.layer.Layer;
import de.dakror.gamesetup.ui.ClickEvent;
import de.dakror.gamesetup.ui.TextButton;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.net.packet.Packet;

/**
 * @author Dakror
 */
public class PauseLayer extends MPLayer
{
	public PauseLayer()
	{
		modal = true;
		Game.player.left = false;
		Game.player.right = false;
		Game.player.up = false;
		Game.player.down = false;
	}
	
	@Override
	public void draw(Graphics2D g)
	{
		Composite c = g.getComposite();
		Color o = g.getColor();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.setColor(Color.black);
		g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
		g.setColor(o);
		g.setComposite(c);
		
		Helper.drawContainer(Game.getWidth() / 2 - 160, Game.getHeight() / 2 - 108, 320, 212, true, false, g);
		
		drawComponents(g);
	}
	
	@Override
	public void update(int tick)
	{
		updateComponents(tick);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) Game.currentGame.removeLayer(this);
	}
	
	@Override
	public void onPacketReceived(Packet p)
	{
		for (Layer l : Game.currentGame.layers)
		{
			if (l instanceof HUDLayer)
			{
				((HUDLayer) l).onPacketReceived(p);
				break;
			}
		}
	}
	
	@Override
	public void init()
	{
		TextButton back = new TextButton(Game.getWidth() / 2 - TextButton.WIDTH / 2, Game.getHeight() / 2 - 90, "Weiter");
		back.addClickEvent(new ClickEvent()
		{
			@Override
			public void trigger()
			{
				Game.currentGame.removeLayer(PauseLayer.this);
			}
		});
		components.add(back);
		
		TextButton settings = new TextButton(Game.getWidth() / 2 - TextButton.WIDTH / 2, Game.getHeight() / 2 - 90 + TextButton.HEIGHT, "Optionen");
		settings.addClickEvent(new ClickEvent()
		{
			@Override
			public void trigger()
			{
				Game.currentGame.addLayer(new SettingsLayer());
			}
		});
		components.add(settings);
		
		TextButton disco = new TextButton(Game.getWidth() / 2 - TextButton.WIDTH / 2, Game.getHeight() / 2 - 90 + TextButton.HEIGHT * 2, "Trennen");
		disco.addClickEvent(new ClickEvent()
		{
			@Override
			public void trigger()
			{
				Game.client.disconnect();
				
				Game.currentGame.setLayer(new MenuLayer());
			}
		});
		components.add(disco);
	}
}
