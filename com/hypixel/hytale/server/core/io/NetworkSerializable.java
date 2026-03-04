package com.hypixel.hytale.server.core.io;

public interface NetworkSerializable<Packet> {
   Packet toPacket();
}
