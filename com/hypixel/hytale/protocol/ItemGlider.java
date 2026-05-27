package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ItemGlider {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   public float terminalVelocity;
   public float fallSpeedMultiplier;
   public float horizontalSpeedMultiplier;
   public float speed;

   public ItemGlider() {
   }

   public ItemGlider(float terminalVelocity, float fallSpeedMultiplier, float horizontalSpeedMultiplier, float speed) {
      this.terminalVelocity = terminalVelocity;
      this.fallSpeedMultiplier = fallSpeedMultiplier;
      this.horizontalSpeedMultiplier = horizontalSpeedMultiplier;
      this.speed = speed;
   }

   public ItemGlider(@Nonnull ItemGlider other) {
      this.terminalVelocity = other.terminalVelocity;
      this.fallSpeedMultiplier = other.fallSpeedMultiplier;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.speed = other.speed;
   }

   @Nonnull
   public static ItemGlider deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 16) {
         throw ProtocolException.bufferTooSmall("ItemGlider", 16, buf.readableBytes() - offset);
      } else {
         ItemGlider obj = new ItemGlider();
         obj.terminalVelocity = buf.getFloatLE(offset + 0);
         obj.fallSpeedMultiplier = buf.getFloatLE(offset + 4);
         obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 8);
         obj.speed = buf.getFloatLE(offset + 12);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 16L;
   }

   public static float getTerminalVelocity(MemorySegment mem) {
      return getTerminalVelocity(mem, 0);
   }

   public static float getTerminalVelocity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getFallSpeedMultiplier(MemorySegment mem) {
      return getFallSpeedMultiplier(mem, 0);
   }

   public static float getFallSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem) {
      return getHorizontalSpeedMultiplier(mem, 0);
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getSpeed(MemorySegment mem) {
      return getSpeed(mem, 0);
   }

   public static float getSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static ItemGlider toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemGlider toObject(MemorySegment mem, int offset) {
      if (offset + 16 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemGlider", offset + 16, (int)mem.byteSize());
      } else {
         return new ItemGlider(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.terminalVelocity);
      buf.writeFloatLE(this.fallSpeedMultiplier);
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.speed);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.terminalVelocity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.fallSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.speed);
      return 16;
   }

   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public ItemGlider clone() {
      ItemGlider copy = new ItemGlider();
      copy.terminalVelocity = this.terminalVelocity;
      copy.fallSpeedMultiplier = this.fallSpeedMultiplier;
      copy.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      copy.speed = this.speed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemGlider other)
            ? false
            : this.terminalVelocity == other.terminalVelocity
               && this.fallSpeedMultiplier == other.fallSpeedMultiplier
               && this.horizontalSpeedMultiplier == other.horizontalSpeedMultiplier
               && this.speed == other.speed;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.terminalVelocity, this.fallSpeedMultiplier, this.horizontalSpeedMultiplier, this.speed);
   }
}
