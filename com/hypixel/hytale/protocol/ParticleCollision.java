package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ParticleCollision {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public ParticleCollisionBlockType blockType = ParticleCollisionBlockType.None;
   @Nonnull
   public ParticleCollisionAction action = ParticleCollisionAction.Expire;
   @Nonnull
   public ParticleRotationInfluence particleRotationInfluence = ParticleRotationInfluence.None;

   public ParticleCollision() {
   }

   public ParticleCollision(
      @Nonnull ParticleCollisionBlockType blockType, @Nonnull ParticleCollisionAction action, @Nonnull ParticleRotationInfluence particleRotationInfluence
   ) {
      this.blockType = blockType;
      this.action = action;
      this.particleRotationInfluence = particleRotationInfluence;
   }

   public ParticleCollision(@Nonnull ParticleCollision other) {
      this.blockType = other.blockType;
      this.action = other.action;
      this.particleRotationInfluence = other.particleRotationInfluence;
   }

   @Nonnull
   public static ParticleCollision deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("ParticleCollision", 3, buf.readableBytes() - offset);
      } else {
         ParticleCollision obj = new ParticleCollision();
         obj.blockType = ParticleCollisionBlockType.fromValue(buf.getByte(offset + 0));
         obj.action = ParticleCollisionAction.fromValue(buf.getByte(offset + 1));
         obj.particleRotationInfluence = ParticleRotationInfluence.fromValue(buf.getByte(offset + 2));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static ParticleCollisionBlockType getBlockType(MemorySegment mem) {
      return getBlockType(mem, 0);
   }

   public static ParticleCollisionBlockType getBlockType(MemorySegment mem, int offset) {
      return ParticleCollisionBlockType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static ParticleCollisionAction getAction(MemorySegment mem) {
      return getAction(mem, 0);
   }

   public static ParticleCollisionAction getAction(MemorySegment mem, int offset) {
      return ParticleCollisionAction.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static ParticleRotationInfluence getParticleRotationInfluence(MemorySegment mem) {
      return getParticleRotationInfluence(mem, 0);
   }

   public static ParticleRotationInfluence getParticleRotationInfluence(MemorySegment mem, int offset) {
      return ParticleRotationInfluence.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static ParticleCollision toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ParticleCollision toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ParticleCollision", offset + 3, (int)mem.byteSize());
      } else {
         return new ParticleCollision(
            ParticleCollisionBlockType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            ParticleCollisionAction.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            ParticleRotationInfluence.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.blockType.getValue());
      buf.writeByte(this.action.getValue());
      buf.writeByte(this.particleRotationInfluence.getValue());
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.blockType.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.action.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.particleRotationInfluence.getValue());
      return 3;
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 3) {
         return ValidationResult.error("Buffer too small: expected at least 3 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid ParticleCollisionBlockType value for BlockType");
         } else {
            v = buffer.getByte(offset + 1) & 255;
            if (v >= 3) {
               return ValidationResult.error("Invalid ParticleCollisionAction value for Action");
            } else {
               v = buffer.getByte(offset + 2) & 255;
               return v >= 5 ? ValidationResult.error("Invalid ParticleRotationInfluence value for ParticleRotationInfluence") : ValidationResult.OK;
            }
         }
      }
   }

   public ParticleCollision clone() {
      ParticleCollision copy = new ParticleCollision();
      copy.blockType = this.blockType;
      copy.action = this.action;
      copy.particleRotationInfluence = this.particleRotationInfluence;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleCollision other)
            ? false
            : Objects.equals(this.blockType, other.blockType)
               && Objects.equals(this.action, other.action)
               && Objects.equals(this.particleRotationInfluence, other.particleRotationInfluence);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockType, this.action, this.particleRotationInfluence);
   }
}
