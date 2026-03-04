package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CraftRecipeAction extends WindowAction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nullable
   public String recipeId;
   public int quantity;

   public CraftRecipeAction() {
   }

   public CraftRecipeAction(@Nullable String recipeId, int quantity) {
      this.recipeId = recipeId;
      this.quantity = quantity;
   }

   public CraftRecipeAction(@Nonnull CraftRecipeAction other) {
      this.recipeId = other.recipeId;
      this.quantity = other.quantity;
   }

   @Nonnull
   public static CraftRecipeAction deserialize(@Nonnull ByteBuf buf, int offset) {
      CraftRecipeAction obj = new CraftRecipeAction();
      byte nullBits = buf.getByte(offset);
      obj.quantity = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int recipeIdLen = VarInt.peek(buf, pos);
         if (recipeIdLen < 0) {
            throw ProtocolException.negativeLength("RecipeId", recipeIdLen);
         }

         if (recipeIdLen > 4096000) {
            throw ProtocolException.stringTooLong("RecipeId", recipeIdLen, 4096000);
         }

         int recipeIdVarLen = VarInt.length(buf, pos);
         obj.recipeId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += recipeIdVarLen + recipeIdLen;
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

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.recipeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.quantity);
      if (this.recipeId != null) {
         PacketIO.writeVarString(buf, this.recipeId, 4096000);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.recipeId != null) {
         size += PacketIO.stringSize(this.recipeId);
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
            int recipeIdLen = VarInt.peek(buffer, pos);
            if (recipeIdLen < 0) {
               return ValidationResult.error("Invalid string length for RecipeId");
            }

            if (recipeIdLen > 4096000) {
               return ValidationResult.error("RecipeId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += recipeIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading RecipeId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public CraftRecipeAction clone() {
      CraftRecipeAction copy = new CraftRecipeAction();
      copy.recipeId = this.recipeId;
      copy.quantity = this.quantity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CraftRecipeAction other) ? false : Objects.equals(this.recipeId, other.recipeId) && this.quantity == other.quantity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.recipeId, this.quantity);
   }
}
