package com.hypixel.hytale.server.core.io;

@FunctionalInterface
public interface NetworkSerializer<Type, Packet> {
   Packet toPacket(Type var1);
}
