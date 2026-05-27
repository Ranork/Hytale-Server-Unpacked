package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.MovementSettings;
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

public class UpdateMovementSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 110;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 252;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 252;
   public static final int MAX_SIZE = 252;
   @Nullable
   public MovementSettings movementSettings;

   @Override
   public int getId() {
      return 110;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateMovementSettings() {
   }

   public UpdateMovementSettings(@Nullable MovementSettings movementSettings) {
      this.movementSettings = movementSettings;
   }

   public UpdateMovementSettings(@Nonnull UpdateMovementSettings other) {
      this.movementSettings = other.movementSettings;
   }

   @Nonnull
   public static UpdateMovementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 252) {
         throw ProtocolException.bufferTooSmall("UpdateMovementSettings", 252, buf.readableBytes() - offset);
      } else {
         UpdateMovementSettings obj = new UpdateMovementSettings();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.movementSettings = MovementSettings.deserialize(buf, offset + 1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 252;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 252L;
   }

   @Nullable
   public static MovementSettings getMovementSettings(MemorySegment mem) {
      return getMovementSettings(mem, 0);
   }

   @Nullable
   public static MovementSettings getMovementSettings(MemorySegment mem, int offset) {
      return hasMovementSettings(mem, offset) ? MovementSettings.toObject(mem, offset + 1) : null;
   }

   public static boolean hasMovementSettings(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateMovementSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateMovementSettings toObject(MemorySegment mem, int offset) {
      if (offset + 252 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateMovementSettings", offset + 252, (int)mem.byteSize());
      } else {
         return new UpdateMovementSettings(hasMovementSettings(mem, offset) ? MovementSettings.toObject(mem, offset + 1) : null);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.movementSettings != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(buf);
      } else {
         buf.writeZero(251);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.movementSettings != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 251L).fill((byte)0);
      }

      return 252;
   }

   @Override
   public int computeSize() {
      return 252;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 252) {
         return ValidationResult.error("Buffer too small: expected at least 252 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public UpdateMovementSettings clone() {
      UpdateMovementSettings copy = new UpdateMovementSettings();
      copy.movementSettings = this.movementSettings != null ? this.movementSettings.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateMovementSettings other ? Objects.equals(this.movementSettings, other.movementSettings) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.movementSettings);
   }
}
