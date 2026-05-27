package com.hypixel.hytale.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;

public interface Packet {
   int getId();

   NetworkChannel getChannel();

   void serialize(@Nonnull ByteBuf var1);

   int serialize(@Nonnull MemorySegment var1, int var2);

   int computeSize();
}
