/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.spamwars.game.entity;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import de.dakror.gamesetup.util.Drawable;
import de.dakror.gamesetup.util.EventListener;
import de.dakror.gamesetup.util.Vector;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.world.Tile;
import de.dakror.spamwars.net.packet.Packet10EntityStatus;


/**
 * @author Dakror
 */
public abstract class Entity extends EventListener implements Drawable {
	public float x, y;
	public int width, height;
	protected boolean gravity;
	protected boolean massive;
	public boolean update;
	protected boolean airborne;
	private boolean enabled;
	
	int life, maxlife;
	
	private Vector velocity;
	
	protected Rectangle bump;
	
	public Entity(float x, float y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		setVelocity(new Vector(0, 0));
		update = true;
		enabled = true;
		massive = true;
	}
	
	protected abstract void tick(int tick);
	
	public abstract void updateServer(int tick);
	
	@Override
	public abstract void draw(Graphics2D g);
	
	public Rectangle getTileSizeBump(float tX, float tY) {
		Rectangle r = new Rectangle();
		r.x = (int) (Math.floor((bump.x + x + tX) / Tile.SIZE) * Tile.SIZE);
		r.y = (int) (Math.floor((bump.y + y + tY) / Tile.SIZE) * Tile.SIZE);
		
		int x2 = (int) (Math.ceil((bump.x + x + tX + bump.width) / Tile.SIZE) * Tile.SIZE);
		int y2 = (int) (Math.ceil((bump.y + y + tY + bump.height) / Tile.SIZE) * Tile.SIZE);
		
		r.width = x2 - r.x;
		r.height = y2 - r.y;
		
		return r;
	}
	
	public Rectangle getGridBump(float tX, float tY) {
		Rectangle r = getTileSizeBump(tX, tY);
		r.x /= Tile.SIZE;
		r.y /= Tile.SIZE;
		r.width /= Tile.SIZE;
		r.height /= Tile.SIZE;
		return r;
	}
	
	@Override
	public void update(int tick) {
		if (Game.world == null) return;
		
		tick(tick);
		
		if (update) {
			float nx = getVelocity().x;
			float ny = getVelocity().y;
			
			Point2D nn = checkAndResolveCollisions(nx, ny);
			nx = (float) nn.getX();
			ny = (float) nn.getY();
			
			if (gravity) affectByGravity();
			else getVelocity().y = 0;
			
			checkAndHandleStomp();
			
			x += nx;
			y += ny;
		}
	}
	
	/**
	 * @return new nx and ny
	 */
	public Point2D checkAndResolveCollisions(float nx, float ny) {
		float x = nx;
		float y = ny;
		
		// -- world -- //
		Rectangle g = getGridBump(nx, ny);
		for (int i = g.x; i < g.x + g.width; i++) {
			for (int j = g.y; j < g.y + g.height; j++) {
				Tile t = Tile.values()[Game.world.getTileId(i, j)];
				
				if (t.getBump() == null && t.getLeftY() < 0) continue;
				
				Rectangle bump = getBump(x, y);
				if ((bump.y + bump.height) % Tile.SIZE == 0) bump.y--;
				
				if (t.getBump() == null) // slope
				{
					float m = (t.getRightY() - t.getLeftY()) / (float) Tile.SIZE;
					
					Polygon p = new Polygon();
					p.addPoint(0, t.getLeftY());
					p.addPoint(Tile.SIZE, t.getRightY());
					p.addPoint(Tile.SIZE, Tile.SIZE);
					p.addPoint(0, Tile.SIZE);
					p.translate(i * Tile.SIZE, j * Tile.SIZE);
					
					int mx = 0;
					int my = 0;
					
					if (p.contains(bump.x, bump.y)) {
						mx = bump.x;
						my = bump.y;
					}
					if (p.contains(bump.x + bump.width, bump.y)) {
						mx = bump.x + bump.width;
						my = bump.y;
					}
					if (p.contains(bump.x, bump.y + bump.height)) {
						mx = bump.x;
						my = bump.y + bump.height;
					}
					if (p.contains(bump.x + bump.width, bump.y + bump.height)) {
						mx = bump.x + bump.width;
						my = bump.y + bump.height;
					}
					
					if (mx + my != 0) {
						int xInSlope = mx % Tile.SIZE;
						int yInSlope = my % Tile.SIZE;
						float yInSlopeM = m * xInSlope;
						
						if (t.getLeftY() < t.getRightY()) y -= yInSlope - yInSlopeM;
						else y -= yInSlope - (Tile.SIZE + yInSlopeM);
						getVelocity().y = 0;
						airborne = false;
					}
				} else {
					Rectangle b = (Rectangle) t.getBump().clone();
					b.translate(i * Tile.SIZE, j * Tile.SIZE);
					
					Rectangle is = bump.intersection(b);
					
					if (is.height < 0 || is.width < 0) continue; // no intersection
					if (is.height <= is.width) {
						y += is.y == bump.y ? is.height : -is.height;
						
						if (is.y == bump.y) {
							airborne = true;
							getVelocity().y = 0;
						} else airborne = false;
					} else x += is.x == bump.x ? is.width : -is.width;
				}
			}
		}
		
		return new Point2D.Float(x, y);
	}
	
	public void checkAndHandleStomp() {
		if (getVelocity().y > 0) {
			Rectangle bump = getBump(velocity.x, velocity.y);
			for (Entity e : Game.world.entities) {
				if (e instanceof Player && !((Player) e).getUser().getUsername().equals(Game.user.getUsername())) {
					if (!e.getBump(0, 0).contains(bump.x, bump.y) && (e.getBump(0, 0).contains(bump.x, bump.y + bump.height) || e.getBump(0, 0).contains(x + bump.width, y + bump.height))) {
						onHitEntity(e, velocity);
						velocity.y = 0;
						airborne = false;
					}
				}
			}
		}
	}
	
	public void affectByGravity() {
		float G = 0.5f;
		
		Rectangle g = getGridBump(0, getVelocity().y + 2 * G);
		Rectangle b = getBump(0, getVelocity().y + 2 * G);
		
		if (Game.world.intersects(g, b)) {
			if (Math.abs(velocity.y) != 0) onHitGround(velocity);
			airborne = false;
			getVelocity().y = 0;
			return;
		}
		//
		// if (Game.world.intersectsEntities(this, b))
		// {
		// airborne = false;
		// getVelocity().y = 0;
		// return;
		// }
		//
		airborne = true;
		getVelocity().y += G;
	}
	
	public Rectangle getBump(float tX, float tY) {
		Rectangle r = (Rectangle) bump.clone();
		r.translate((int) x, (int) y);
		r.translate((int) tX, (int) tY);
		
		return r;
	}
	
	public Vector getPos() {
		return new Vector(x, y);
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}
	
	public void setPos(Vector position) {
		x = position.x;
		y = position.y;
	}
	
	public int getLife() {
		return life;
	}
	
	public void setLife(int life) {
		this.life = life;
	}
	
	public int getMaxlife() {
		return maxlife;
	}
	
	public void setMaxlife(int maxlife) {
		this.maxlife = maxlife;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled, boolean send, boolean server) {
		this.enabled = enabled;
		if (send) {
			try {
				if (server) Game.server.sendPacketToAllClients(new Packet10EntityStatus(getPos(), enabled));
				else Game.client.sendPacket(new Packet10EntityStatus(getPos(), enabled));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isMassive() {
		return massive;
	}
	
	public void setMassive(boolean m) {
		massive = m;
	}
	
	// -- abstract event methods -- //
	
	protected void onHitGround(Vector velocity) {}
	
	protected void onHitEntity(Entity e, Vector velocity) {}
	
}
