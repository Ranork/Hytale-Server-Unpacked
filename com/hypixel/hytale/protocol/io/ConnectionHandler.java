package com.hypixel.hytale.protocol.io;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.ToServerPacket;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ConnectionHandler {
   void handle(@Nonnull ToServerPacket var1);

   void closed(@Nullable NetworkChannel var1);

   void logCloseMessage();

   void registered(@Nullable ConnectionHandler var1);

   void unregistered(@Nullable ConnectionHandler var1);
}
