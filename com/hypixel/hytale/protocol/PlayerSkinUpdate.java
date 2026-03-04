package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerSkinUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 327680184;
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
      PlayerSkinUpdate obj = new PlayerSkinUpdate();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         obj.skin = PlayerSkin.deserialize(buf, pos);
         pos += PlayerSkin.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += PlayerSkin.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
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
