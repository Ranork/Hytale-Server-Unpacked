package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSkinUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 2104;
   @Nullable
   public PlayerSkin skin;

   public PlayerSkinUpdate() {
   }

   public PlayerSkinUpdate(@Nullable PlayerSkin skin) {
      this.skin = skin;
   }

   public PlayerSkinUpdate(@Nonnull PlayerSkinUpdate other) {
      this.skin = other.skin;
   }

   @Nonnull
   public static PlayerSkinUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("PlayerSkinUpdate", 1, buf.readableBytes() - offset);
      } else {
         PlayerSkinUpdate obj = new PlayerSkinUpdate();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            obj.skin = PlayerSkin.deserialize(buf, pos);
            pos += PlayerSkin.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += PlayerSkin.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static PlayerSkin getSkin(MemorySegment mem) {
      return getSkin(mem, 0);
   }

   @Nullable
   public static PlayerSkin getSkin(MemorySegment mem, int offset) {
      return hasSkin(mem, offset) ? PlayerSkin.toObject(mem, offset + 1) : null;
   }

   public static boolean hasSkin(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static PlayerSkinUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PlayerSkinUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PlayerSkinUpdate", offset + 1, (int)mem.byteSize());
      } else {
         return new PlayerSkinUpdate(hasSkin(mem, offset) ? PlayerSkin.toObject(mem, offset + 1) : null);
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.skin != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.skin != null) {
         this.skin.serialize(buf);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.skin != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.skin != null) {
         varOffset += this.skin.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.skin != null) {
         size += this.skin.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            ValidationResult skinResult = PlayerSkin.validateStructure(buffer, pos);
            if (!skinResult.isValid()) {
               return ValidationResult.error("Invalid Skin: " + skinResult.error());
            }

            pos += PlayerSkin.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public PlayerSkinUpdate clone() {
      PlayerSkinUpdate copy = new PlayerSkinUpdate();
      copy.skin = this.skin != null ? this.skin.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof PlayerSkinUpdate other ? Objects.equals(this.skin, other.skin) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.skin);
   }
}
