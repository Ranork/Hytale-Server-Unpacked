package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorFetchAssetReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 312;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 4096010;
   public int token;
   @Nullable
   public byte[] contents;

   @Override
   public int getId() {
      return 312;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorFetchAssetReply() {
   }

   public AssetEditorFetchAssetReply(int token, @Nullable byte[] contents) {
      this.token = token;
      this.contents = contents;
   }

   public AssetEditorFetchAssetReply(@Nonnull AssetEditorFetchAssetReply other) {
      this.token = other.token;
      this.contents = other.contents;
   }

   @Nonnull
   public static AssetEditorFetchAssetReply deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorFetchAssetReply obj = new AssetEditorFetchAssetReply();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int contentsCount = VarInt.peek(buf, pos);
         if (contentsCount < 0) {
            throw ProtocolException.negativeLength("Contents", contentsCount);
         }

         if (contentsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Contents", contentsCount, 4096000);
         }

         int contentsVarLen = VarInt.size(contentsCount);
         if (pos + contentsVarLen + contentsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Contents", pos + contentsVarLen + contentsCount * 1, buf.readableBytes());
         }

         pos += contentsVarLen;
         obj.contents = new byte[contentsCount];

         for (int i = 0; i < contentsCount; i++) {
            obj.contents[i] = buf.getByte(pos + i * 1);
         }

         pos += contentsCount * 1;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 5;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.contents != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.contents != null) {
         if (this.contents.length > 4096000) {
            throw ProtocolException.arrayTooLong("Contents", this.contents.length, 4096000);
         }

         VarInt.write(buf, this.contents.length);

         for (byte item : this.contents) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 5;
      if (this.contents != null) {
         size += VarInt.size(this.contents.length) + this.contents.length * 1;
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
            int contentsCount = VarInt.peek(buffer, pos);
            if (contentsCount < 0) {
               return ValidationResult.error("Invalid array count for Contents");
            }

            if (contentsCount > 4096000) {
               return ValidationResult.error("Contents exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += contentsCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Contents");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFetchAssetReply clone() {
      AssetEditorFetchAssetReply copy = new AssetEditorFetchAssetReply();
      copy.token = this.token;
      copy.contents = this.contents != null ? Arrays.copyOf(this.contents, this.contents.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFetchAssetReply other) ? false : this.token == other.token && Arrays.equals(this.contents, other.contents);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.token);
      return 31 * result + Arrays.hashCode(this.contents);
   }
}
