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


package de.dakror.spamwars.game;

import de.dakror.gamesetup.Updater;
import de.dakror.spamwars.net.packet.Packet03Attribute;

/**
 * @author Dakror
 */
public class ServerUpdater extends Thread {
	public int tick, ticks;
	public long time, time2;
	
	public int countdown = -1;
	
	public int speed = 1;
	
	public boolean closeRequested = false;
	
	public ServerUpdater() {
		setName("ServerUpdater-Thread");
		setPriority(Thread.MAX_PRIORITY);
		start();
	}
	
	@Override
	public void run() {
		tick = 0;
		time = System.currentTimeMillis();
		while (!closeRequested) {
			if (Game.server == null) break;
			if (tick == Integer.MAX_VALUE) tick = 0;
			
			if (Game.server.world != null) Game.server.world.updateServer(tick);
			
			if (countdown > -1 && time2 == 0) {
				time2 = System.currentTimeMillis();
				try {
					Game.server.sendPacketToAllClients(new Packet03Attribute("countdown", countdown));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (System.currentTimeMillis() - time2 >= 1000 && time2 > 0) {
				if (countdown <= 0) {
					time2 = 0;
					countdown = -1;
				} else {
					countdown--;
					try {
						Game.server.sendPacketToAllClients(new Packet03Attribute("countdown", countdown));
					} catch (Exception e) {
						e.printStackTrace();
					}
					time2 = System.currentTimeMillis();
				}
			}
			
			try {
				tick++;
				ticks++;
				Thread.sleep(Math.round(Updater.TIMEOUT / (float) speed));
			} catch (InterruptedException e) {}
		}
	}
}
