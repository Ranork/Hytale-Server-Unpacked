package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UpdateForcedMusic implements Packet, ToClientPacket {
   public static final int PACKET_ID = 151;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int containerIndex;

   @Override
   public int getId() {
      return 151;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateForcedMusic() {
   }

   public UpdateForcedMusic(int containerIndex) {
      this.containerIndex = containerIndex;
   }

   public UpdateForcedMusic(@Nonnull UpdateForcedMusic other) {
      this.containerIndex = other.containerIndex;
   }

   @Nonnull
   public static UpdateForcedMusic deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("UpdateForcedMusic", 4, buf.readableBytes() - offset);
      } else {
         UpdateForcedMusic obj = new UpdateForcedMusic();
         obj.containerIndex = buf.getIntLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static int getContainerIndex(MemorySegment mem) {
      return getContainerIndex(mem, 0);
   }

   public static int getContainerIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static UpdateForcedMusic toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateForcedMusic toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateForcedMusic", offset + 4, (int)mem.byteSize());
      } else {
         return new UpdateForcedMusic(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.containerIndex);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.containerIndex);
      return 4;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public UpdateForcedMusic clone() {
      UpdateForcedMusic copy = new UpdateForcedMusic();
      copy.containerIndex = this.containerIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateForcedMusic other ? this.containerIndex == other.containerIndex : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.containerIndex);
   }
}
