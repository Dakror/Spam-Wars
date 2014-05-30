package de.dakror.spamwars.ui.weaponry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.dakror.gamesetup.ui.ClickableComponent;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.weapon.Part;
import de.dakror.spamwars.game.weapon.WeaponData;

/**
 * @author Dakror
 */
public class WeaponryWeaponButton extends ClickableComponent
{
	public static final int WIDTH = 450;
	public static final int HEIGHT = 300;
	
	public WeaponData data;
	BufferedImage image;
	public int id;
	
	public boolean selected;
	
	public WeaponryWeaponButton(int x, int y, WeaponData data)
	{
		super(x, y, WIDTH, HEIGHT);
		this.data = data;
		this.data.calculateStats();
		image = data.getImage();
		Dimension dim = Helper.scaleTo(new Dimension(image.getWidth(), image.getHeight()), new Dimension(width - 30, height - 30));
		if (dim.width < image.getWidth() || dim.height < image.getHeight()) image = Helper.toBufferedImage(image.getScaledInstance(dim.width, dim.height, BufferedImage.SCALE_SMOOTH));
		selected = false;
	}
	
	@Override
	public void draw(Graphics2D g)
	{
		if (!selected)
		{
			Helper.drawShadow(x, y, width, height, g);
			Helper.drawOutline(x, y, width, height, state != 0, g);
		}
		else
		{
			Helper.drawContainer(x, y, width, height, true, false, g);
		}
		g.drawImage(image, x + (width - image.getWidth()) / 2, y + (height - image.getHeight()) / 2, Game.w);
	}
	
	@Override
	public void update(int tick)
	{}
	
	@Override
	public void drawTooltip(int x, int y, Graphics2D g)
	{
		if (data == null) return;
		
		int size = 190, height = 170;
		Helper.drawShadow(x, y, size, height, g);
		Helper.drawOutline(x, y, size, height, false, g);
		
		Color c = g.getColor();
		g.setColor(Color.black);
		Helper.drawProgressBar(x + 15, y + 15, size - 30, data.getSpeed() / (float) Part.highest_speed, "7a36a3", g);
		Helper.drawHorizontallyCenteredString("Verzögerung", x, size, y + 31, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 35, size - 30, data.getMagazine() / (float) Part.highest_magazine, "ffc744", g);
		Helper.drawHorizontallyCenteredString("Munition", x, size, y + 51, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 55, size - 30, data.getAngle() / (float) Part.highest_angle, "009ab8", g);
		Helper.drawHorizontallyCenteredString("Winkel", x, size, y + 71, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 75, size - 30, data.getReload() / (float) Part.highest_reload, "a55212", g);
		Helper.drawHorizontallyCenteredString("Nachladen", x, size, y + 91, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 95, size - 30, data.getProjectileSpeed() / (float) Part.highest_projectileSpeed, "2a86e7", g);
		Helper.drawHorizontallyCenteredString("Schnelligkeit", x, size, y + 111, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 115, size - 30, data.getRange() / (float) Part.highest_range, "7dd33c", g);
		Helper.drawHorizontallyCenteredString("Reichweite", x, size, y + 131, g, 15);
		
		Helper.drawProgressBar(x + 15, y + 135, size - 30, data.getDamage() / (float) Part.highest_damage, "ff3232", g);
		Helper.drawHorizontallyCenteredString("Schaden", x, size, y + 151, g, 15);
		g.setColor(c);
	}
}