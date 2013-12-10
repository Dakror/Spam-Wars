package de.dakror.spamwars.game.weapon;

import java.awt.Point;
import java.awt.Rectangle;

import de.dakror.gamesetup.util.Vector;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.projectile.Projectile;
import de.dakror.spamwars.game.projectile.ProjectileType;

/**
 * @author Dakror
 */
public class Pistol extends Weapon
{
	public Pistol()
	{
		super(new Rectangle(629, 1125, 212, 129), new Point(841, 1147), new Point(659, 1212), FireMode.SINGLE, 10, 50, 9, 90, 35);
		type = WeaponType.PISTOL;
	}
	
	@Override
	protected Projectile getPojectile(Vector pos, Vector target)
	{
		return new Projectile(pos, target, Game.user.getUsername(), ProjectileType.DEFAUL_LEAD);
	}
}