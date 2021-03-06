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

package de.dakror.spamwars.net.packet;

/**
 * @author Dakror
 */
public class Packet02Reject extends Packet {
    public static enum Cause {
        OUTDATEDCLIENT("Your Client runs on an older version than the server."),
        OUTDATEDSERVER("The server runs on an older version than your client."),
        USERNAMETAKEN("This username is already taken on that server.."),
        GAMERUNNING("The game already started on this server."),
        FULL("This server is full."),

        ;
        private String description;

        private Cause(String desc) {
            description = desc;
        }

        public String getDescription() {
            return description;
        }
    }

    private Cause cause;

    public Packet02Reject(Cause cause) {
        super(2);
        this.cause = cause;
    }

    public Packet02Reject(byte[] data) {
        super(2);
        cause = Cause.values()[Integer.parseInt(readData(data))];
    }

    public Cause getCause() {
        return cause;
    }

    @Override
    public byte[] getPacketData() {
        return (cause.ordinal() + "").getBytes();
    }
}
