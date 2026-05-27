package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;

public class BuilderToolSelectionToolAskForClipboard implements Packet, ToServerPacket {
   public static final int PACKET_ID = 410;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 0;

   @Override
   public int getId() {
      return 410;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   @Nonnull
   public static BuilderToolSelectionToolAskForClipboard deserialize(@Nonnull ByteBuf buf, int offset) {
      return new BuilderToolSelectionToolAskForClipboard();
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 0;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static BuilderToolSelectionToolAskForClipboard toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSelectionToolAskForClipboard toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionToolAskForClipboard", offset + 0, (int)mem.byteSize());
      } else {
         return new BuilderToolSelectionToolAskForClipboard();
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

   public BuilderToolSelectionToolAskForClipboard clone() {
      return new BuilderToolSelectionToolAskForClipboard();
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof BuilderToolSelectionToolAskForClipboard other;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}
