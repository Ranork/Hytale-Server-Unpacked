package com.hypixel.hytale.server.core.io.adapter;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.function.BiConsumer;

public interface PlayerPacketWatcher extends BiConsumer<PlayerRef, Packet> {
   void accept(PlayerRef var1, Packet var2);
}
