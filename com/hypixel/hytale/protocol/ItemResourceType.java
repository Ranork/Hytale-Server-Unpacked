package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemResourceType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String id;
   public int quantity = 1;

   public ItemResourceType() {
   }

   public ItemResourceType(@Nullable String id, int quantity) {
      this.id = id;
      this.quantity = quantity;
   }

   public ItemResourceType(@Nonnull ItemResourceType other) {
      this.id = other.id;
      this.quantity = other.quantity;
   }

   @Nonnull
   public static ItemResourceType deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemResourceType obj = new ItemResourceType();
      byte nullBits = buf.getByte(offset);
      obj.quantity = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int idLen = VarInt.peek(buf, pos);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         int idVarLen = VarInt.length(buf, pos);
         obj.id = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += idVarLen + idLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.quantity);
      if (this.id != null) {
         PacketIO.writeVarString(buf, this.id, 4096000);
      }
   }

   public int computeSize() {
      int size = 5;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
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
            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemResourceType clone() {
      ItemResourceType copy = new ItemResourceType();
      copy.id = this.id;
      copy.quantity = this.quantity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemResourceType other) ? false : Objects.equals(this.id, other.id) && this.quantity == other.quantity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.quantity);
   }
}
