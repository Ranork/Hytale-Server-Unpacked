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

public class ModelTexture {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String texture;
   public float weight;

   public ModelTexture() {
   }

   public ModelTexture(@Nullable String texture, float weight) {
      this.texture = texture;
      this.weight = weight;
   }

   public ModelTexture(@Nonnull ModelTexture other) {
      this.texture = other.texture;
      this.weight = other.weight;
   }

   @Nonnull
   public static ModelTexture deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("ModelTexture", 5, buf.readableBytes() - offset);
      } else {
         ModelTexture obj = new ModelTexture();
         byte nullBits = buf.getByte(offset);
         obj.weight = buf.getFloatLE(offset + 1);
         int pos = offset + 5;
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
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset) ? PacketIO.readVarString("Texture", mem, offset + 5, 4096000, PacketIO.UTF8) : null;
   }

   public static float getWeight(MemorySegment mem) {
      return getWeight(mem, 0);
   }

   public static float getWeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ModelTexture toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelTexture toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelTexture", offset + 5, (int)mem.byteSize());
      } else {
         return new ModelTexture(
            hasTexture(mem, offset) ? PacketIO.readVarString("Texture", mem, offset + 5, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.texture != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.weight);
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
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.weight);
      int varOffset = offset + 5;
      if (this.texture != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 5;
      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int textureLen = VarInt.peek(buffer, pos);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            pos += VarInt.size(textureLen);
            pos += textureLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelTexture clone() {
      ModelTexture copy = new ModelTexture();
      copy.texture = this.texture;
      copy.weight = this.weight;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelTexture other) ? false : Objects.equals(this.texture, other.texture) && this.weight == other.weight;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.texture, this.weight);
   }
}
