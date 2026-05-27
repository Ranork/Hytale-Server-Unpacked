package com.hypixel.hytale.server.core.io;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.Hitbox;

public interface NetworkSerializers {
   NetworkSerializer<Box, Hitbox> BOX = t -> {
      Hitbox packet = new Hitbox();
      packet.minX = (float)t.getMin().x();
      packet.minY = (float)t.getMin().y();
      packet.minZ = (float)t.getMin().z();
      packet.maxX = (float)t.getMax().x();
      packet.maxY = (float)t.getMax().y();
      packet.maxZ = (float)t.getMax().z();
      return packet;
   };
}
