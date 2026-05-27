package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ChargingDelay {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public float minDelay;
   public float maxDelay;
   public float maxTotalDelay;
   public float minHealth;
   public float maxHealth;

   public ChargingDelay() {
   }

   public ChargingDelay(float minDelay, float maxDelay, float maxTotalDelay, float minHealth, float maxHealth) {
      this.minDelay = minDelay;
      this.maxDelay = maxDelay;
      this.maxTotalDelay = maxTotalDelay;
      this.minHealth = minHealth;
      this.maxHealth = maxHealth;
   }

   public ChargingDelay(@Nonnull ChargingDelay other) {
      this.minDelay = other.minDelay;
      this.maxDelay = other.maxDelay;
      this.maxTotalDelay = other.maxTotalDelay;
      this.minHealth = other.minHealth;
      this.maxHealth = other.maxHealth;
   }

   @Nonnull
   public static ChargingDelay deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 20) {
         throw ProtocolException.bufferTooSmall("ChargingDelay", 20, buf.readableBytes() - offset);
      } else {
         ChargingDelay obj = new ChargingDelay();
         obj.minDelay = buf.getFloatLE(offset + 0);
         obj.maxDelay = buf.getFloatLE(offset + 4);
         obj.maxTotalDelay = buf.getFloatLE(offset + 8);
         obj.minHealth = buf.getFloatLE(offset + 12);
         obj.maxHealth = buf.getFloatLE(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 20L;
   }

   public static float getMinDelay(MemorySegment mem) {
      return getMinDelay(mem, 0);
   }

   public static float getMinDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getMaxDelay(MemorySegment mem) {
      return getMaxDelay(mem, 0);
   }

   public static float getMaxDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getMaxTotalDelay(MemorySegment mem) {
      return getMaxTotalDelay(mem, 0);
   }

   public static float getMaxTotalDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getMinHealth(MemorySegment mem) {
      return getMinHealth(mem, 0);
   }

   public static float getMinHealth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getMaxHealth(MemorySegment mem) {
      return getMaxHealth(mem, 0);
   }

   public static float getMaxHealth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static ChargingDelay toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ChargingDelay toObject(MemorySegment mem, int offset) {
      if (offset + 20 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ChargingDelay", offset + 20, (int)mem.byteSize());
      } else {
         return new ChargingDelay(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.minDelay);
      buf.writeFloatLE(this.maxDelay);
      buf.writeFloatLE(this.maxTotalDelay);
      buf.writeFloatLE(this.minHealth);
      buf.writeFloatLE(this.maxHealth);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.minDelay);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.maxDelay);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.maxTotalDelay);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.minHealth);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.maxHealth);
      return 20;
   }

   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public ChargingDelay clone() {
      ChargingDelay copy = new ChargingDelay();
      copy.minDelay = this.minDelay;
      copy.maxDelay = this.maxDelay;
      copy.maxTotalDelay = this.maxTotalDelay;
      copy.minHealth = this.minHealth;
      copy.maxHealth = this.maxHealth;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ChargingDelay other)
            ? false
            : this.minDelay == other.minDelay
               && this.maxDelay == other.maxDelay
               && this.maxTotalDelay == other.maxTotalDelay
               && this.minHealth == other.minHealth
               && this.maxHealth == other.maxHealth;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minDelay, this.maxDelay, this.maxTotalDelay, this.minHealth, this.maxHealth);
   }
}
