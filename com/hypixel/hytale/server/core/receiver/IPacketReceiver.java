package com.hypixel.hytale.server.core.receiver;

import com.hypixel.hytale.protocol.ToClientPacket;
import javax.annotation.Nonnull;

public interface IPacketReceiver {
   void write(@Nonnull ToClientPacket var1);

   void writeNoCache(@Nonnull ToClientPacket var1);
}
