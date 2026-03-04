package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public interface IPacketHandler {
   void registerHandler(int var1, @Nonnull Consumer<ToServerPacket> var2);

   void registerNoOpHandlers(int... var1);

   @Nonnull
   PlayerRef getPlayerRef();

   @Nonnull
   String getIdentifier();
}
