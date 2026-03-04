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

public class AssetEditorActivateButton implements Packet, ToServerPacket {
   public static final int PACKET_ID = 335;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String buttonId;

   @Override
   public int getId() {
      return 335;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorActivateButton() {
   }

   public AssetEditorActivateButton(@Nullable String buttonId) {
      this.buttonId = buttonId;
   }

   public AssetEditorActivateButton(@Nonnull AssetEditorActivateButton other) {
      this.buttonId = other.buttonId;
   }

   @Nonnull
   public static AssetEditorActivateButton deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorActivateButton obj = new AssetEditorActivateButton();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int buttonIdLen = VarInt.peek(buf, pos);
         if (buttonIdLen < 0) {
            throw ProtocolException.negativeLength("ButtonId", buttonIdLen);
         }

         if (buttonIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ButtonId", buttonIdLen, 4096000);
         }

         int buttonIdVarLen = VarInt.length(buf, pos);
         obj.buttonId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += buttonIdVarLen + buttonIdLen;
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
      if (this.buttonId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.buttonId != null) {
         PacketIO.writeVarString(buf, this.buttonId, 4096000);
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.buttonId != null) {
         size += PacketIO.stringSize(this.buttonId);
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
            int buttonIdLen = VarInt.peek(buffer, pos);
            if (buttonIdLen < 0) {
               return ValidationResult.error("Invalid string length for ButtonId");
            }

            if (buttonIdLen > 4096000) {
               return ValidationResult.error("ButtonId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += buttonIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ButtonId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorActivateButton clone() {
      AssetEditorActivateButton copy = new AssetEditorActivateButton();
      copy.buttonId = this.buttonId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorActivateButton other ? Objects.equals(this.buttonId, other.buttonId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.buttonId);
   }
}
