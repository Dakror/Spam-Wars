package de.dakror.spamwars.game.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import de.dakror.gamesetup.util.Helper;
import de.dakror.gamesetup.util.Vector;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.weapon.Handgun;
import de.dakror.spamwars.game.weapon.Weapon;
import de.dakror.spamwars.net.User;
import de.dakror.spamwars.net.packet.Packet5PlayerData;


/**
 * @author Dakror
 */
public class Player extends Entity
{
	public boolean left, right, up, down;
	
	boolean lookingLeft = false;
	
	private int style = 0;
	
	private Weapon weapon;
	
	/**
	 * 0 stand, 0-10 = walking, 11 = jump
	 */
	public int frame = 0;
	
	Point hand = new Point(0, 0);
	
	Point mouse = new Point(0, 0);
	
	User user;
	
	public Player(float x, float y, User user)
	{
		super(x, y, 72, 97);
		
		style = (int) (Math.random() * 3 + 1);
		bump = new Rectangle(10, 7, 44, 84);
		gravity = true;
		
		this.user = user;
		
		setWeapon(new Handgun());
	}
	
	@Override
	public void draw(Graphics2D g)
	{
		float mx = x + Game.world.x;
		float my = y + Game.world.y;
		
		Font oldF = g.getFont();
		g.setFont(new Font("", Font.PLAIN, 25));
		Color o = g.getColor();
		g.setColor(Color.darkGray);
		Helper.drawHorizontallyCenteredString(user.getUsername(), (int) mx, width, (int) my - 5, g, 20);
		g.setFont(oldF);
		g.setColor(o);
		
		AffineTransform old = g.getTransform();
		if (lookingLeft)
		{
			AffineTransform at = g.getTransform();
			at.translate((mx + width / 2) * 2, 0);
			at.scale(-1, 1);
			g.setTransform(at);
		}
		
		if (frame >= 0 && frame <= 10)
		{
			String frame = (this.frame + 1) + "";
			if (frame.length() == 1) frame = "0" + frame;
			
			g.drawImage(Game.getImage("entity/player/p" + getStyle() + "/p" + getStyle() + "_walk" + frame + ".png"), (int) mx, (int) my, Game.w);
		}
		else if (frame == 11)
		{
			g.drawImage(Game.getImage("entity/player/p" + getStyle() + "/p" + getStyle() + "_jump.png"), (int) mx, (int) my, Game.w);
		}
		g.setTransform(old);
		
		old = g.getTransform();
		AffineTransform at = g.getTransform();
		at.translate(hand.x + mx, hand.y + my);
		g.setTransform(at);
		
		getWeapon().draw(g);
		
		g.setTransform(old);
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		if (!user.getUsername().equals(Game.user.getUsername())) return;
		
		lookingLeft = e.getX() < x + width / 2;
		mouse = e.getPoint();
		
		Vector dif = new Vector(e.getPoint()).sub(getWeaponPoint());
		
		getWeapon().rot2 = (float) Math.toRadians(dif.getAngleOnXAxis() * (lookingLeft ? -1 : 1));
	}
	
	public Vector getWeaponPoint()
	{
		Vector exit = new Vector(getWeapon().getExit()).mul(Weapon.scale);
		exit.x = 0;
		
		Vector point = getPos().add(new Vector(hand)).sub(new Vector(getWeapon().getGrab()).mul(Weapon.scale)).add(exit);
		
		return point;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (!user.getUsername().equals(Game.user.getUsername())) return;
		
		lookingLeft = e.getX() < x + width / 2;
		mouse = e.getPoint();
		
		Vector dif = new Vector(e.getPoint()).sub(getWeaponPoint());
		
		getWeapon().rot2 = (float) Math.toRadians(dif.getAngleOnXAxis() * (lookingLeft ? -1 : 1));
		
		getWeapon().shoot(new Vector(e.getPoint()));
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if (!user.getUsername().equals(Game.user.getUsername())) return;
		
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
			case KeyEvent.VK_SPACE:
			{
				if (!airborne) getVelocity().y = -15;
				up = true;
				break;
			}
			// case KeyEvent.VK_SHIFT:
			// {
			// down = true;
			// break;
			// }
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		if (!user.getUsername().equals(Game.user.getUsername())) return;
		
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
			case KeyEvent.VK_SPACE:
			{
				up = false;
				break;
			}
			// case KeyEvent.VK_S:
			// {
			// down = false;
			// break;
			// }
		}
	}
	
	@Override
	protected void tick(int tick)
	{
		int speed = airborne ? 3 : 4;
		
		if (user.getUsername().equals(Game.user.getUsername()))
		{
			if (left) getVelocity().x = -speed;
			if (right) getVelocity().x = speed;
			if (!airborne && getVelocity().x != 0 && tick % 4 == 0)
			{
				frame = frame < 0 ? 0 : frame;
				
				frame = (frame + 1) % 6;
			}
			else if (airborne)
			{
				frame = 11;
			}
			
			if (!left && !right)
			{
				frame = 3;
				getVelocity().x = 0;
			}
			
			int mx = (Game.getWidth() - width) / 2;
			int my = (Game.getHeight() - height) / 2;
			
			if (x > mx && Game.world.width - x > (Game.getWidth() + width) / 2) Game.world.x = -x + mx;
			if (y > my) Game.world.y = -y + my;
		}
		
		getWeapon().left = lookingLeft;
		if (lookingLeft) hand = new Point(0, 60);
		else hand = new Point(65, 60);
		
		try
		{
			if (user.getUsername().equals(Game.user.getUsername())) Game.client.sendPacket(new Packet5PlayerData(this));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getStyle()
	{
		return style;
	}
	
	public void setStyle(int style2)
	{
		style = style2;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public Weapon getWeapon()
	{
		return weapon;
	}
	
	public void setWeapon(Weapon weapon)
	{
		this.weapon = weapon;
	}
	
}
