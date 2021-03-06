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

package de.dakror.spamwars.ui.weaponry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import de.dakror.gamesetup.ui.ClickableComponent;
import de.dakror.gamesetup.util.Helper;
import de.dakror.spamwars.game.Game;
import de.dakror.spamwars.game.weapon.Part;

/**
 * @author Dakror
 */
public class WeaponryButton extends ClickableComponent {
    public static final int SIZE = 128;

    BufferedImage icon;

    public boolean selected;
    public boolean loseSelectionOnRMB;
    Part part;

    public WeaponryButton(Rectangle icon) {
        super(0, 0, SIZE, SIZE);
        this.icon = Game.getImage("weapon/explode.png").getSubimage(icon.x, icon.y, icon.width, icon.height);

        Dimension dim = Helper.scaleTo(new Dimension(icon.width, icon.height), new Dimension(SIZE - 30, SIZE - 30));
        selected = false;
        this.icon = Helper.toBufferedImage(this.icon.getScaledInstance(dim.width, dim.height, BufferedImage.SCALE_SMOOTH));

        loseSelectionOnRMB = false;
    }

    @Override
    public void draw(Graphics2D g) {
        if (state == 0 && !selected) {
            if (enabled) Helper.drawShadow(x, y, width, height, g);
            Helper.drawOutline(x, y, width, height, false, g);
        } else Helper.drawContainer(x, y, width, height, false, state == 1 || selected, g);

        g.drawImage(icon, x + (width - icon.getWidth()) / 2, y + (height - icon.getHeight()) / 2, Game.w);

        int m = 9;

        if (part != null) {
            Color c = g.getColor();
            g.setColor(Color.decode("#c48813"));

            Helper.drawRightAlignedString((part.price == 0) ? "Frei" : part.price + "$", x + width - 10, y + height - 10, g, 15);

            g.setColor(c);
        }

        if (!enabled) Helper.drawShadow(x - m, y - m, width + m * 2, height + m * 2, g);
    }

    @Override
    public void update(int tick) {}

    @Override
    public void mouseReleased(MouseEvent e) {
        if (contains(e.getX(), e.getY()) && enabled) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (selected) {
                    selected = false;
                    state = 2;
                    return;
                }

                triggerEvents();
                selected = true;
            } else if (e.getButton() == MouseEvent.BUTTON3 && loseSelectionOnRMB) selected = false;
        }
    }

    @Override
    public void drawTooltip(int x, int y, Graphics2D g) {
        if (part == null) return;

        int size = 190, height = 170;
        Helper.drawShadow(x, y, size, height, g);
        Helper.drawOutline(x, y, size, height, false, g);

        Color c = g.getColor();
        g.setColor(Color.black);
        Helper.drawProgressBar(x + 15, y + 15, size - 30, part.speed / (float) Part.highest_speed, "7a36a3", g);
        Helper.drawHorizontallyCenteredString("Delay", x, size, y + 31, g, 15);

        Helper.drawProgressBar(x + 15, y + 35, size - 30, part.magazine / (float) Part.highest_magazine, "ffc744", g);
        Helper.drawHorizontallyCenteredString("Ammo", x, size, y + 51, g, 15);

        Helper.drawProgressBar(x + 15, y + 55, size - 30, part.angle / (float) Part.highest_angle, "009ab8", g);
        Helper.drawHorizontallyCenteredString("Angle", x, size, y + 71, g, 15);

        Helper.drawProgressBar(x + 15, y + 75, size - 30, part.reload / (float) Part.highest_reload, "a55212", g);
        Helper.drawHorizontallyCenteredString("Reload", x, size, y + 91, g, 15);

        Helper.drawProgressBar(x + 15, y + 95, size - 30, part.projectileSpeed / (float) Part.highest_projectileSpeed, "2a86e7", g);
        Helper.drawHorizontallyCenteredString("Speed", x, size, y + 111, g, 15);

        Helper.drawProgressBar(x + 15, y + 115, size - 30, part.range / (float) Part.highest_range, "7dd33c", g);
        Helper.drawHorizontallyCenteredString("Range", x, size, y + 131, g, 15);

        Helper.drawProgressBar(x + 15, y + 135, size - 30, part.damage / (float) Part.highest_damage, "ff3232", g);
        Helper.drawHorizontallyCenteredString("Damage", x, size, y + 151, g, 15);
        g.setColor(c);
    }

    public void setPart(Part part) {
        this.part = part;
    }
}
