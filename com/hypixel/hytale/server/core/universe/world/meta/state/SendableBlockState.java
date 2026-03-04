package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.List;

@Deprecated
public interface SendableBlockState {
   void sendTo(List<ToClientPacket> var1);

   void unloadFrom(List<ToClientPacket> var1);

   default boolean canPlayerSee(PlayerRef player) {
      return true;
   }
}
