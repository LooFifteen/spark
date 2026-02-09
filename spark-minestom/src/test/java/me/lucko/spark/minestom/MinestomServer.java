/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.minestom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;

public final class MinestomServer {

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init(new Auth.Online());
        MinecraftServer.setBrandName("LU15");

        // initialize Spark
        Path directory = Path.of("spark");
        SparkMinestom spark = SparkMinestom.builder(directory)
                .commands(true)
                .permissionHandler((sender, permission) -> true)
                .enable();

        // set up Minestom
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        EventNode<Event> eventNode = MinecraftServer.getGlobalEventHandler();
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 0));
        });

        // register shutdown hook to delete the temp directory
        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            try {
                spark.shutdown();
                Files.deleteIfExists(directory);
            } catch (IOException ignored) {
                // oh well...
            }
        });

        OpenToLAN.open();

        server.start("0.0.0.0", 25565);
    }

}
