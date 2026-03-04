package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SwitchHotbarBlockSet implements Packet, ToServerPacket {
   public static final int PACKET_ID = 178;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String itemId;

   @Override
   public int getId() {
      return 178;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SwitchHotbarBlockSet() {
   }

   public SwitchHotbarBlockSet(@Nullable String itemId) {
      this.itemId = itemId;
   }

   public SwitchHotbarBlockSet(@Nonnull SwitchHotbarBlockSet other) {
      this.itemId = other.itemId;
   }

   @Nonnull
   public static SwitchHotbarBlockSet deserialize(@Nonnull ByteBuf buf, int offset) {
      SwitchHotbarBlockSet obj = new SwitchHotbarBlockSet();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int itemIdLen = VarInt.peek(buf, pos);
         if (itemIdLen < 0) {
            throw ProtocolException.negativeLength("ItemId", itemIdLen);
         }

         if (itemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
         }

         int itemIdVarLen = VarInt.length(buf, pos);
         obj.itemId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += itemIdVarLen + itemIdLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.itemId != null) {
         PacketIO.writeVarString(buf, this.itemId, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.itemId != null) {
         size += PacketIO.stringSize(this.itemId);
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
            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SwitchHotbarBlockSet clone() {
      SwitchHotbarBlockSet copy = new SwitchHotbarBlockSet();
      copy.itemId = this.itemId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SwitchHotbarBlockSet other ? Objects.equals(this.itemId, other.itemId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId);
   }
}
