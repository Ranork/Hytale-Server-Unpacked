package com.hypixel.hytale.server.core.io;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.Hitbox;

public interface NetworkSerializers {
   NetworkSerializer<Box, Hitbox> BOX = t -> {
      Hitbox packet = new Hitbox();
      packet.minX = (float)t.getMin().getX();
      packet.minY = (float)t.getMin().getY();
      packet.minZ = (float)t.getMin().getZ();
      packet.maxX = (float)t.getMax().getX();
      packet.maxY = (float)t.getMax().getY();
      packet.maxZ = (float)t.getMax().getZ();
      return packet;
   };
}
