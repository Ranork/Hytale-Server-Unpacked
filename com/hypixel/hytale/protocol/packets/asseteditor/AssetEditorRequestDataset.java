package com.hypixel.hytale.protocol.packets.asseteditor;

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

public class AssetEditorRequestDataset implements Packet, ToServerPacket {
   public static final int PACKET_ID = 333;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String name;

   @Override
   public int getId() {
      return 333;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorRequestDataset() {
   }

   public AssetEditorRequestDataset(@Nullable String name) {
      this.name = name;
   }

   public AssetEditorRequestDataset(@Nonnull AssetEditorRequestDataset other) {
      this.name = other.name;
   }

   @Nonnull
   public static AssetEditorRequestDataset deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorRequestDataset obj = new AssetEditorRequestDataset();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int nameLen = VarInt.peek(buf, pos);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         int nameVarLen = VarInt.length(buf, pos);
         obj.name = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += nameVarLen + nameLen;
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
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.name != null) {
         PacketIO.writeVarString(buf, this.name, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
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
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorRequestDataset clone() {
      AssetEditorRequestDataset copy = new AssetEditorRequestDataset();
      copy.name = this.name;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorRequestDataset other ? Objects.equals(this.name, other.name) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.name);
   }
}
