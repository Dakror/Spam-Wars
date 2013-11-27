package de.dakror.spamwars.game.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import de.dakror.spamwars.game.Game;


/**
 * @author Dakror
 */
public class Player extends Entity
{
	boolean left, right, up, down;
	
	public Player(float x, float y)
	{
		super(x, y, 72, 97);
		
		bump = new Rectangle(10, 7, 44, 84);
		gravity = true;
	}
	
	@Override
	public void draw(Graphics2D g)
	{
		g.drawImage(Game.getImage("entity/player/p1/p1_front.png"), (int) x, (int) y, Game.w);
		drawBump(g);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_A:
			{
				left = true;
				break;
			}
			case KeyEvent.VK_D:
			{
				right = true;
				break;
			}
			case KeyEvent.VK_W:
			{
				velocity.y = -15;
				up = true;
				break;
			}
			case KeyEvent.VK_S:
			{
				down = true;
				break;
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_A:
			{
				left = false;
				break;
			}
			case KeyEvent.VK_D:
			{
				right = false;
				break;
			}
			case KeyEvent.VK_W:
			{
				up = false;
				break;
			}
			case KeyEvent.VK_S:
			{
				down = false;
				break;
			}
		}
	}
	
	@Override
	protected void tick(int tick)
	{
		int speed = airborne ? 6 : 8;
		
		if (left) velocity.x = -speed;
		if (right) velocity.x = speed;
		
		if (!left && !right) velocity.x = 0;
		// if (up) ny -= speed;
		// if (down) ny += speed;
	}
}
