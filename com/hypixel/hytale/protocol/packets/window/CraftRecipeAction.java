package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("CraftRecipeAction", 5, buf.readableBytes() - offset);
      } else {
         CraftRecipeAction obj = new CraftRecipeAction();
         byte nullBits = buf.getByte(offset);
         obj.quantity = buf.getIntLE(offset + 1);
         int pos = offset + 5;
         if ((nullBits & 1) != 0) {
            int recipeIdLen = VarInt.peek(buf, pos);
            if (recipeIdLen < 0) {
               throw ProtocolException.invalidVarInt("RecipeId");
            }

            int recipeIdVarLen = VarInt.size(recipeIdLen);
            if (recipeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("RecipeId", recipeIdLen, 4096000);
            }

            if (pos + recipeIdVarLen + recipeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RecipeId", pos + recipeIdVarLen + recipeIdLen, buf.readableBytes());
            }

            obj.recipeId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += recipeIdVarLen + recipeIdLen;
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
   public static String getRecipeId(MemorySegment mem) {
      return getRecipeId(mem, 0);
   }

   @Nullable
   public static String getRecipeId(MemorySegment mem, int offset) {
      return hasRecipeId(mem, offset) ? PacketIO.readVarString("RecipeId", mem, offset + 5, 4096000, PacketIO.UTF8) : null;
   }

   public static int getQuantity(MemorySegment mem) {
      return getQuantity(mem, 0);
   }

   public static int getQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean hasRecipeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static CraftRecipeAction toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CraftRecipeAction toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CraftRecipeAction", offset + 5, (int)mem.byteSize());
      } else {
         return new CraftRecipeAction(
            hasRecipeId(mem, offset) ? PacketIO.readVarString("RecipeId", mem, offset + 5, 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1))
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.recipeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.quantity);
      int varOffset = offset + 5;
      if (this.recipeId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.recipeId, 4096000);
      }

      return varOffset - offset;
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

            pos += VarInt.size(recipeIdLen);
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
