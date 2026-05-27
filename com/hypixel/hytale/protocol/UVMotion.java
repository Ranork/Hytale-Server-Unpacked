package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UVMotion {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 16384024;
   @Nullable
   public String texture;
   public boolean addRandomUVOffset;
   public float speedX;
   public float speedY;
   public float scale;
   public float strength;
   @Nonnull
   public UVMotionCurveType strengthCurveType = UVMotionCurveType.Constant;

   public UVMotion() {
   }

   public UVMotion(
      @Nullable String texture,
      boolean addRandomUVOffset,
      float speedX,
      float speedY,
      float scale,
      float strength,
      @Nonnull UVMotionCurveType strengthCurveType
   ) {
      this.texture = texture;
      this.addRandomUVOffset = addRandomUVOffset;
      this.speedX = speedX;
      this.speedY = speedY;
      this.scale = scale;
      this.strength = strength;
      this.strengthCurveType = strengthCurveType;
   }

   public UVMotion(@Nonnull UVMotion other) {
      this.texture = other.texture;
      this.addRandomUVOffset = other.addRandomUVOffset;
      this.speedX = other.speedX;
      this.speedY = other.speedY;
      this.scale = other.scale;
      this.strength = other.strength;
      this.strengthCurveType = other.strengthCurveType;
   }

   @Nonnull
   public static UVMotion deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 19) {
         throw ProtocolException.bufferTooSmall("UVMotion", 19, buf.readableBytes() - offset);
      } else {
         UVMotion obj = new UVMotion();
         byte nullBits = buf.getByte(offset);
         obj.addRandomUVOffset = buf.getByte(offset + 1) != 0;
         obj.speedX = buf.getFloatLE(offset + 2);
         obj.speedY = buf.getFloatLE(offset + 6);
         obj.scale = buf.getFloatLE(offset + 10);
         obj.strength = buf.getFloatLE(offset + 14);
         obj.strengthCurveType = UVMotionCurveType.fromValue(buf.getByte(offset + 18));
         int pos = offset + 19;
         if ((nullBits & 1) != 0) {
            int textureLen = VarInt.peek(buf, pos);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (pos + textureVarLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", pos + textureVarLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += textureVarLen + textureLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 19;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 19L;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset) ? PacketIO.readVarString("Texture", mem, offset + 19, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean getAddRandomUVOffset(MemorySegment mem) {
      return getAddRandomUVOffset(mem, 0);
   }

   public static boolean getAddRandomUVOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static float getSpeedX(MemorySegment mem) {
      return getSpeedX(mem, 0);
   }

   public static float getSpeedX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2));
   }

   public static float getSpeedY(MemorySegment mem) {
      return getSpeedY(mem, 0);
   }

   public static float getSpeedY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6));
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10));
   }

   public static float getStrength(MemorySegment mem) {
      return getStrength(mem, 0);
   }

   public static float getStrength(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static UVMotionCurveType getStrengthCurveType(MemorySegment mem) {
      return getStrengthCurveType(mem, 0);
   }

   public static UVMotionCurveType getStrengthCurveType(MemorySegment mem, int offset) {
      return UVMotionCurveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 18)));
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UVMotion toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UVMotion toObject(MemorySegment mem, int offset) {
      if (offset + 19 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UVMotion", offset + 19, (int)mem.byteSize());
      } else {
         return new UVMotion(
            hasTexture(mem, offset) ? PacketIO.readVarString("Texture", mem, offset + 19, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14)),
            UVMotionCurveType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 18)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.addRandomUVOffset ? 1 : 0);
      buf.writeFloatLE(this.speedX);
      buf.writeFloatLE(this.speedY);
      buf.writeFloatLE(this.scale);
      buf.writeFloatLE(this.strength);
      buf.writeByte(this.strengthCurveType.getValue());
      if (this.texture != null) {
         PacketIO.writeVarString(buf, this.texture, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.addRandomUVOffset);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 2), this.speedX);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 6), this.speedY);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 10), this.scale);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.strength);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 18), (byte)this.strengthCurveType.getValue());
      int varOffset = offset + 19;
      if (this.texture != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 19;
      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 19) {
         return ValidationResult.error("Buffer too small: expected at least 19 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 18) & 255;
         if (v >= 9) {
            return ValidationResult.error("Invalid UVMotionCurveType value for StrengthCurveType");
         } else {
            v = offset + 19;
            if ((nullBits & 1) != 0) {
               int textureLen = VarInt.peek(buffer, v);
               if (textureLen < 0) {
                  return ValidationResult.error("Invalid string length for Texture");
               }

               if (textureLen > 4096000) {
                  return ValidationResult.error("Texture exceeds max length 4096000");
               }

               v += VarInt.size(textureLen);
               v += textureLen;
               if (v > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Texture");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UVMotion clone() {
      UVMotion copy = new UVMotion();
      copy.texture = this.texture;
      copy.addRandomUVOffset = this.addRandomUVOffset;
      copy.speedX = this.speedX;
      copy.speedY = this.speedY;
      copy.scale = this.scale;
      copy.strength = this.strength;
      copy.strengthCurveType = this.strengthCurveType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UVMotion other)
            ? false
            : Objects.equals(this.texture, other.texture)
               && this.addRandomUVOffset == other.addRandomUVOffset
               && this.speedX == other.speedX
               && this.speedY == other.speedY
               && this.scale == other.scale
               && this.strength == other.strength
               && Objects.equals(this.strengthCurveType, other.strengthCurveType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.addRandomUVOffset, this.speedX, this.speedY, this.scale, this.strength, this.strengthCurveType);
   }
}
