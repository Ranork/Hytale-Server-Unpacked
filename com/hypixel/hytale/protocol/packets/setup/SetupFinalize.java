package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;

public class SetupFinalize implements Packet, ToServerPacket {
   public static final int PACKET_ID = 33;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 0;

   @Override
   public int getId() {
      return 33;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   @Nonnull
   public static SetupFinalize deserialize(@Nonnull ByteBuf buf, int offset) {
      return new SetupFinalize();
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 0;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static SetupFinalize toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetupFinalize toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetupFinalize", offset + 0, (int)mem.byteSize());
      } else {
         return new SetupFinalize();
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      return 0;
   }

   @Override
   public int computeSize() {
      return 0;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 0 ? ValidationResult.error("Buffer too small: expected at least 0 bytes") : ValidationResult.OK;
   }

   public SetupFinalize clone() {
      return new SetupFinalize();
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof SetupFinalize other;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}
