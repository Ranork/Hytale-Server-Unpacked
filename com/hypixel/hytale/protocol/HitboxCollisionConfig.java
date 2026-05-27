package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class HitboxCollisionConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   @Nonnull
   public CollisionType collisionType = CollisionType.Hard;
   public float softCollisionOffsetRatio;

   public HitboxCollisionConfig() {
   }

   public HitboxCollisionConfig(@Nonnull CollisionType collisionType, float softCollisionOffsetRatio) {
      this.collisionType = collisionType;
      this.softCollisionOffsetRatio = softCollisionOffsetRatio;
   }

   public HitboxCollisionConfig(@Nonnull HitboxCollisionConfig other) {
      this.collisionType = other.collisionType;
      this.softCollisionOffsetRatio = other.softCollisionOffsetRatio;
   }

   @Nonnull
   public static HitboxCollisionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("HitboxCollisionConfig", 5, buf.readableBytes() - offset);
      } else {
         HitboxCollisionConfig obj = new HitboxCollisionConfig();
         obj.collisionType = CollisionType.fromValue(buf.getByte(offset + 0));
         obj.softCollisionOffsetRatio = buf.getFloatLE(offset + 1);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static CollisionType getCollisionType(MemorySegment mem) {
      return getCollisionType(mem, 0);
   }

   public static CollisionType getCollisionType(MemorySegment mem, int offset) {
      return CollisionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static float getSoftCollisionOffsetRatio(MemorySegment mem) {
      return getSoftCollisionOffsetRatio(mem, 0);
   }

   public static float getSoftCollisionOffsetRatio(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static HitboxCollisionConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static HitboxCollisionConfig toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("HitboxCollisionConfig", offset + 5, (int)mem.byteSize());
      } else {
         return new HitboxCollisionConfig(
            CollisionType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.collisionType.getValue());
      buf.writeFloatLE(this.softCollisionOffsetRatio);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.collisionType.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.softCollisionOffsetRatio);
      return 5;
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         return v >= 2 ? ValidationResult.error("Invalid CollisionType value for CollisionType") : ValidationResult.OK;
      }
   }

   public HitboxCollisionConfig clone() {
      HitboxCollisionConfig copy = new HitboxCollisionConfig();
      copy.collisionType = this.collisionType;
      copy.softCollisionOffsetRatio = this.softCollisionOffsetRatio;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof HitboxCollisionConfig other)
            ? false
            : Objects.equals(this.collisionType, other.collisionType) && this.softCollisionOffsetRatio == other.softCollisionOffsetRatio;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.collisionType, this.softCollisionOffsetRatio);
   }
}
