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

import java.awt.Color;
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
import de.dakror.spamwars.game.anim.Animation;
import de.dakror.spamwars.game.projectile.Projectile;
import de.dakror.spamwars.game.weapon.Action;
import de.dakror.spamwars.game.weapon.Weapon;
import de.dakror.spamwars.game.weapon.WeaponData;
import de.dakror.spamwars.game.weapon.WeaponType;
import de.dakror.spamwars.game.world.Tile;
import de.dakror.spamwars.layer.RespawnLayer;
import de.dakror.spamwars.net.User;
import de.dakror.spamwars.net.packet.Packet06PlayerData;
import de.dakror.spamwars.net.packet.Packet09Kill;
import de.dakror.spamwars.net.packet.Packet11GameInfo.GameMode;
import de.dakror.spamwars.net.packet.Packet12Stomp;

/**
 * @author Dakror
 */
public class Player extends Entity {
    public boolean left, right, up, down;

    public boolean lookingLeft = false;

    private int style = 0;

    private Weapon weapon;

    /**
     * 0 stand, 0-10 = walking, 11 = jump
     */
    public int frame = 0;

    Point hand = new Point(0, 0);

    Point mouse = new Point(0, 0);

    User user;

    public Player(float x, float y, User user) {
        super(x, y, 72, 97);

        style = (int) (Math.random() * 3 + 1);
        gravity = true;
        bump = new Rectangle(11, 7, 44, 84);

        this.user = user;

        life = maxlife = 100;

        if (user.getUsername().equals(Game.user.getUsername())) setWeapon(Game.activeWeapon);
    }

    @Override
    public void draw(Graphics2D g) {
        if (Game.world == null) return;
        if (weapon == null) return;

        float mx = x + Game.world.x;
        float my = y + Game.world.y;

        Color o = g.getColor();
        g.setColor(Color.darkGray);
        Helper.drawHorizontallyCenteredString(user.getUsername(), (int) mx, width, (int) my - 5, g, 20);

        if (!user.getUsername().equals(Game.user.getUsername())) Helper.drawProgressBar((int) mx, (int) (my + height - 5), width, life / (float) maxlife, "ff3232", g);
        g.setColor(o);

        AffineTransform old = g.getTransform();
        if (lookingLeft) {
            AffineTransform at = g.getTransform();
            at.translate((mx + width / 2) * 2, 0);
            at.scale(-1, 1);
            g.setTransform(at);
        }

        if (frame >= 0 && frame <= 10) {
            String frame = (this.frame + 1) + "";
            if (frame.length() == 1) frame = "0" + frame;

            g.drawImage(Game.getImage("entity/player/p" + getStyle() + "/p" + getStyle() + "_walk" + frame + ".png"), (int) mx, (int) my, Game.w);
        } else if (frame == 11) {
            g.drawImage(Game.getImage("entity/player/p" + getStyle() + "/p" + getStyle() + "_jump.png"), (int) mx, (int) my, Game.w);
        }
        g.setTransform(old);

        old = g.getTransform();
        AffineTransform at = g.getTransform();
        at.translate(hand.x + mx, hand.y + my);
        g.setTransform(at);

        weapon.draw(g);

        g.setTransform(old);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0) return;

        handleMouse(e, false);
    }

    public Vector getWeaponPoint() {
        if (weapon == null) return null;
        Vector exit = new Vector(weapon.getExit()).mul(Weapon.scale);
        exit.x = 0;

        Vector point = getPos().add(new Vector(hand)).sub(new Vector(weapon.getGrab()).mul(Weapon.scale)).add(exit);

        return point;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0) return;

        if (e.getButton() == MouseEvent.BUTTON1) handleMouse(e, true);
    }

    public void handleMouse(MouseEvent e, boolean target) {
        if (weapon == null) return;

        lookingLeft = e.getX() < x + width / 2;
        mouse = e.getPoint();

        Vector dif = new Vector(e.getPoint()).sub(getWeaponPoint());

        weapon.rot2 = (float) Math.toRadians(dif.getAngleOnXAxis() * (lookingLeft ? -1 : 1));

        if (target) {
            Point p = new Point((int) x + hand.x, (int) y + hand.y);
            Point tile = Game.world.getTile(p.x, p.y);
            Tile t = Tile.values()[Game.world.getTileIdAtPixel(p.x, p.y)];

            if (t.getBump() != null) {
                Rectangle r = (Rectangle) t.getBump().clone();
                r.translate(tile.x * Tile.SIZE, tile.y * Tile.SIZE);
                if (r.contains(p)) return;
            }

            weapon.target(new Vector(e.getPoint()));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0 || !weapon.getData().isAutomatic()) return;

        if (e.getModifiers() == MouseEvent.BUTTON1_MASK) handleMouse(e, true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0) return;

        weapon.target(null);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: {
                left = true;
                break;
            }
            case KeyEvent.VK_D: {
                right = true;
                break;
            }
            case KeyEvent.VK_SPACE: {
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
    public void keyReleased(KeyEvent e) {
        if (!user.getUsername().equals(Game.user.getUsername()) || life <= 0) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: {
                left = false;
                break;
            }
            case KeyEvent.VK_D: {
                right = false;
                break;
            }
            case KeyEvent.VK_SPACE: {
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
    protected void tick(int tick) {
        if (weapon == null) return;

        int speed = airborne ? 6 : 10;

        if (user.getUsername().equals(Game.user.getUsername())) {
            if (left) getVelocity().x = -speed;
            if (right) getVelocity().x = speed;
            if (!airborne && getVelocity().x != 0 && tick % 4 == 0) {
                frame = frame < 0 ? 0 : frame;

                frame = (frame + 1) % 6;
            } else if (airborne) {
                frame = 11;
            }

            if (!left && !right) {
                frame = 3;
                getVelocity().x = 0;
            }

            int mx = (Game.getWidth() - width) / 2;
            int my = (Game.getHeight() - height) / 2;

            float oldWorldX = Game.world.x;
            float oldWorldY = Game.world.y;

            if (life > 0) {

                Game.world.x = mx - x;
                Game.world.y = my - y;

                float fx = Game.world.x + Game.world.width;

                if (fx < Game.getWidth() && fx > 0) Game.world.x += Game.getWidth() - fx;
                else if (Game.world.x > 0) Game.world.x = 0;

                if (lookingLeft) bump = new Rectangle(11, 7, 44, 84);
                else bump = new Rectangle(15, 7, 44, 84);
            } else {
                left = right = up = down = false;
                weapon.target(null);
            }

            if (weapon.getTarget() != null) {
                weapon.getTarget().x -= Game.world.x - oldWorldX;
                weapon.getTarget().y -= Game.world.y - oldWorldY;
            }
        }

        weapon.left = lookingLeft;
        weapon.update(tick);
        if (lookingLeft) hand = new Point(0, 60);
        else hand = new Point(65, 60);

        try {
            if (user.getUsername().equals(Game.user.getUsername()) && tick % 2 == 0) Game.client.sendPacket(new Packet06PlayerData(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style2) {
        style = style2;
    }

    public User getUser() {
        return user;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(WeaponData data) {
        try {
            data.calculateStats();
            weapon = new Weapon(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void revive() {
        gravity = true;
        Vector spawn = Game.world.getBestSpawnPoint();
        x = spawn.x * Tile.SIZE + (int) (Math.random() * Tile.SIZE - Tile.SIZE / 2);
        y = spawn.y * Tile.SIZE - height + Tile.SIZE + (int) (Math.random() * Tile.SIZE - Tile.SIZE / 2);
        life = maxlife;
        weapon.ammo = weapon.magazine;
        weapon.capacity = weapon.capacityMax;
    }

    public void dealDamage(float damage, Object source) {
        life -= damage;

        if (Game.client.gameInfo.getGameMode() == GameMode.ONE_IN_THE_CHAMBER && source instanceof Projectile) life = 0;

        if (life <= 0 && x > -10000000) {
            if (source instanceof Projectile) {
                try {
                    Game.client.sendPacket(new Packet09Kill(((Projectile) source).getUsername(), Game.user.getUsername(), WeaponType.WEAPON));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (source instanceof Action) {
                try {
                    Game.client.sendPacket(new Packet09Kill(((Action) source).username, Game.user.getUsername(), ((Action) source).type));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Game.world.addAnimation(new Animation("expl/11", getPos().clone().sub(new Vector((192 - width) / 2, (192 - height) / 2)), 2, 192, 24), true);
            x = -10000000;
            gravity = false;
            life = 0;
            Game.currentGame.addLayer(new RespawnLayer());
        }
    }

    @Override
    public void updateServer(int tick) {
        if (weapon == null) return;
        for (Entity e : Game.server.world.entities) {
            if (e.isEnabled() && e.getBump(0, 0).intersects(getBump(0, 0))) {
                if (weapon.canRefill() && e instanceof AmmoBox) {
                    e.setEnabled(false, true, true);
                    weapon.refill(AmmoBox.AMMO);
                    try {
                        Game.server.sendPacketToAllClients(new Packet06PlayerData(this));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                if (life < maxlife && e instanceof HealthBox) {
                    e.setEnabled(false, true, true);
                    life = life + HealthBox.HEALTH > maxlife ? maxlife : life + HealthBox.HEALTH;
                    try {
                        Game.server.sendPacketToAllClients(new Packet06PlayerData(this));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public void stop() {
        left = false;
        right = false;
        up = false;
        down = false;
    }

    @Override
    protected void onHitGround(Vector velocity) {
        float maxFall = 20;
        float dmgFactor = 0.25f;
        if (velocity.y > maxFall) dealDamage((float) Math.pow((velocity.y - maxFall), 2) * dmgFactor, new Action(WeaponType.FALL_DAMAGE, Game.user.getUsername()));
    }

    @Override
    protected void onHitEntity(Entity e, Vector velocity) {
        if (e instanceof Player && velocity.y > 5) {
            try {
                Game.client.sendPacket(new Packet12Stomp(Game.user.getUsername(), ((Player) e).getUser().getUsername(), velocity.y * 2));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
