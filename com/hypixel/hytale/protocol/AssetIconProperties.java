package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector2fc;
import org.joml.Vector3fc;

public class AssetIconProperties {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   public float scale;
   @Nullable
   public Vector2fc translation;
   @Nullable
   public Vector3fc rotation;

   public AssetIconProperties() {
   }

   public AssetIconProperties(float scale, @Nullable Vector2fc translation, @Nullable Vector3fc rotation) {
      this.scale = scale;
      this.translation = translation;
      this.rotation = rotation;
   }

   public AssetIconProperties(@Nonnull AssetIconProperties other) {
      this.scale = other.scale;
      this.translation = other.translation;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static AssetIconProperties deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("AssetIconProperties", 25, buf.readableBytes() - offset);
      } else {
         AssetIconProperties obj = new AssetIconProperties();
         byte nullBits = buf.getByte(offset);
         obj.scale = buf.getFloatLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.translation = PacketIO.readVector2f(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            obj.rotation = PacketIO.readVector3f(buf, offset + 13);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   public static float getScale(MemorySegment mem) {
      return getScale(mem, 0);
   }

   public static float getScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   @Nullable
   public static Vector2fc getTranslation(MemorySegment mem) {
      return getTranslation(mem, 0);
   }

   @Nullable
   public static Vector2fc getTranslation(MemorySegment mem, int offset) {
      return hasTranslation(mem, offset) ? PacketIO.readVector2f(mem, offset + 5) : null;
   }

   @Nullable
   public static Vector3fc getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Vector3fc getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null;
   }

   public static boolean hasTranslation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static AssetIconProperties toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetIconProperties toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetIconProperties", offset + 25, (int)mem.byteSize());
      } else {
         return new AssetIconProperties(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            hasTranslation(mem, offset) ? PacketIO.readVector2f(mem, offset + 5) : null,
            hasRotation(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.scale);
      if (this.translation != null) {
         PacketIO.writeVector2f(buf, this.translation);
      } else {
         buf.writeZero(8);
      }

      if (this.rotation != null) {
         PacketIO.writeVector3f(buf, this.rotation);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.translation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.scale);
      if (this.translation != null) {
         PacketIO.writeVector2f(mem, offset + 5, this.translation);
      } else {
         mem.asSlice(offset + 5, 8L).fill((byte)0);
      }

      if (this.rotation != null) {
         PacketIO.writeVector3f(mem, offset + 13, this.rotation);
      } else {
         mem.asSlice(offset + 13, 12L).fill((byte)0);
      }

      return 25;
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public AssetIconProperties clone() {
      AssetIconProperties copy = new AssetIconProperties();
      copy.scale = this.scale;
      copy.translation = this.translation;
      copy.rotation = this.rotation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetIconProperties other)
            ? false
            : this.scale == other.scale && Objects.equals(this.translation, other.translation) && Objects.equals(this.rotation, other.rotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.scale, this.translation, this.rotation);
   }
}
