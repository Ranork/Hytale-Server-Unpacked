package com.hypixel.hytale.server.core.io.adapter;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.util.function.BiPredicate;

public interface PacketFilter extends BiPredicate<PacketHandler, Packet> {
   boolean test(PacketHandler var1, Packet var2);
}
