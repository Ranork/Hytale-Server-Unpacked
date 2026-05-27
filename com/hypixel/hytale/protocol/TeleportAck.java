package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class TeleportAck {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public byte teleportId;

   public TeleportAck() {
   }

   public TeleportAck(byte teleportId) {
      this.teleportId = teleportId;
   }

   public TeleportAck(@Nonnull TeleportAck other) {
      this.teleportId = other.teleportId;
   }

   @Nonnull
   public static TeleportAck deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("TeleportAck", 1, buf.readableBytes() - offset);
      } else {
         TeleportAck obj = new TeleportAck();
         obj.teleportId = buf.getByte(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static byte getTeleportId(MemorySegment mem) {
      return getTeleportId(mem, 0);
   }

   public static byte getTeleportId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
   }

   public static TeleportAck toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TeleportAck toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TeleportAck", offset + 1, (int)mem.byteSize());
      } else {
         return new TeleportAck(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.teleportId);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), this.teleportId);
      return 1;
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public TeleportAck clone() {
      TeleportAck copy = new TeleportAck();
      copy.teleportId = this.teleportId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TeleportAck other ? this.teleportId == other.teleportId : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.teleportId);
   }
}
