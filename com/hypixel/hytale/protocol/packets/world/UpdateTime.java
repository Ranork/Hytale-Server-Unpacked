package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.InstantData;
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
import javax.annotation.Nullable;

public class UpdateTime implements Packet, ToClientPacket {
   public static final int PACKET_ID = 146;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 13;
   @Nullable
   public InstantData gameTime;

   @Override
   public int getId() {
      return 146;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateTime() {
   }

   public UpdateTime(@Nullable InstantData gameTime) {
      this.gameTime = gameTime;
   }

   public UpdateTime(@Nonnull UpdateTime other) {
      this.gameTime = other.gameTime;
   }

   @Nonnull
   public static UpdateTime deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("UpdateTime", 13, buf.readableBytes() - offset);
      } else {
         UpdateTime obj = new UpdateTime();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.gameTime = InstantData.deserialize(buf, offset + 1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static InstantData getGameTime(MemorySegment mem) {
      return getGameTime(mem, 0);
   }

   @Nullable
   public static InstantData getGameTime(MemorySegment mem, int offset) {
      return hasGameTime(mem, offset) ? InstantData.toObject(mem, offset + 1) : null;
   }

   public static boolean hasGameTime(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateTime toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateTime toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateTime", offset + 13, (int)mem.byteSize());
      } else {
         return new UpdateTime(hasGameTime(mem, offset) ? InstantData.toObject(mem, offset + 1) : null);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.gameTime != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.gameTime != null) {
         this.gameTime.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.gameTime != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.gameTime != null) {
         this.gameTime.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      return 13;
   }

   @Override
   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public UpdateTime clone() {
      UpdateTime copy = new UpdateTime();
      copy.gameTime = this.gameTime != null ? this.gameTime.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateTime other ? Objects.equals(this.gameTime, other.gameTime) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.gameTime);
   }
}
