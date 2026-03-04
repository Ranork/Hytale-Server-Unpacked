package com.hypixel.hytale.protocol;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public interface Packet {
   int getId();

   NetworkChannel getChannel();

   void serialize(@Nonnull ByteBuf var1);

   int computeSize();
}
