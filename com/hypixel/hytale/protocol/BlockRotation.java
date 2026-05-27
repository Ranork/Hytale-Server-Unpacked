package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockRotation {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public Rotation rotationYaw = Rotation.None;
   @Nonnull
   public Rotation rotationPitch = Rotation.None;
   @Nonnull
   public Rotation rotationRoll = Rotation.None;

   public BlockRotation() {
   }

   public BlockRotation(@Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      this.rotationYaw = rotationYaw;
      this.rotationPitch = rotationPitch;
      this.rotationRoll = rotationRoll;
   }

   public BlockRotation(@Nonnull BlockRotation other) {
      this.rotationYaw = other.rotationYaw;
      this.rotationPitch = other.rotationPitch;
      this.rotationRoll = other.rotationRoll;
   }

   @Nonnull
   public static BlockRotation deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 3) {
         throw ProtocolException.bufferTooSmall("BlockRotation", 3, buf.readableBytes() - offset);
      } else {
         BlockRotation obj = new BlockRotation();
         obj.rotationYaw = Rotation.fromValue(buf.getByte(offset + 0));
         obj.rotationPitch = Rotation.fromValue(buf.getByte(offset + 1));
         obj.rotationRoll = Rotation.fromValue(buf.getByte(offset + 2));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 3L;
   }

   public static Rotation getRotationYaw(MemorySegment mem) {
      return getRotationYaw(mem, 0);
   }

   public static Rotation getRotationYaw(MemorySegment mem, int offset) {
      return Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static Rotation getRotationPitch(MemorySegment mem) {
      return getRotationPitch(mem, 0);
   }

   public static Rotation getRotationPitch(MemorySegment mem, int offset) {
      return Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static Rotation getRotationRoll(MemorySegment mem) {
      return getRotationRoll(mem, 0);
   }

   public static Rotation getRotationRoll(MemorySegment mem, int offset) {
      return Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static BlockRotation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockRotation toObject(MemorySegment mem, int offset) {
      if (offset + 3 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockRotation", offset + 3, (int)mem.byteSize());
      } else {
         return new BlockRotation(
            Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            Rotation.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.rotationYaw.getValue());
      buf.writeByte(this.rotationPitch.getValue());
      buf.writeByte(this.rotationRoll.getValue());
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.rotationYaw.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.rotationPitch.getValue());
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.rotationRoll.getValue());
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
            return ValidationResult.error("Invalid Rotation value for RotationYaw");
         } else {
            v = buffer.getByte(offset + 1) & 255;
            if (v >= 4) {
               return ValidationResult.error("Invalid Rotation value for RotationPitch");
            } else {
               v = buffer.getByte(offset + 2) & 255;
               return v >= 4 ? ValidationResult.error("Invalid Rotation value for RotationRoll") : ValidationResult.OK;
            }
         }
      }
   }

   public BlockRotation clone() {
      BlockRotation copy = new BlockRotation();
      copy.rotationYaw = this.rotationYaw;
      copy.rotationPitch = this.rotationPitch;
      copy.rotationRoll = this.rotationRoll;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockRotation other)
            ? false
            : Objects.equals(this.rotationYaw, other.rotationYaw)
               && Objects.equals(this.rotationPitch, other.rotationPitch)
               && Objects.equals(this.rotationRoll, other.rotationRoll);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.rotationYaw, this.rotationPitch, this.rotationRoll);
   }
}
