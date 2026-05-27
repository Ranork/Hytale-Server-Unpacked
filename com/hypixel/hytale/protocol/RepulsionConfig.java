package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class RepulsionConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float radius;
   public float minForce;
   public float maxForce;

   public RepulsionConfig() {
   }

   public RepulsionConfig(float radius, float minForce, float maxForce) {
      this.radius = radius;
      this.minForce = minForce;
      this.maxForce = maxForce;
   }

   public RepulsionConfig(@Nonnull RepulsionConfig other) {
      this.radius = other.radius;
      this.minForce = other.minForce;
      this.maxForce = other.maxForce;
   }

   @Nonnull
   public static RepulsionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("RepulsionConfig", 12, buf.readableBytes() - offset);
      } else {
         RepulsionConfig obj = new RepulsionConfig();
         obj.radius = buf.getFloatLE(offset + 0);
         obj.minForce = buf.getFloatLE(offset + 4);
         obj.maxForce = buf.getFloatLE(offset + 8);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static float getRadius(MemorySegment mem) {
      return getRadius(mem, 0);
   }

   public static float getRadius(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getMinForce(MemorySegment mem) {
      return getMinForce(mem, 0);
   }

   public static float getMinForce(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getMaxForce(MemorySegment mem) {
      return getMaxForce(mem, 0);
   }

   public static float getMaxForce(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static RepulsionConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RepulsionConfig toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RepulsionConfig", offset + 12, (int)mem.byteSize());
      } else {
         return new RepulsionConfig(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.radius);
      buf.writeFloatLE(this.minForce);
      buf.writeFloatLE(this.maxForce);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.radius);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.minForce);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.maxForce);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public RepulsionConfig clone() {
      RepulsionConfig copy = new RepulsionConfig();
      copy.radius = this.radius;
      copy.minForce = this.minForce;
      copy.maxForce = this.maxForce;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RepulsionConfig other)
            ? false
            : this.radius == other.radius && this.minForce == other.minForce && this.maxForce == other.maxForce;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.radius, this.minForce, this.maxForce);
   }
}
