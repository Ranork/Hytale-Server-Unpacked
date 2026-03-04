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

public class AssetEditorFetchAutoCompleteData implements Packet, ToServerPacket {
   public static final int PACKET_ID = 331;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 32768023;
   public int token;
   @Nullable
   public String dataset;
   @Nullable
   public String query;

   @Override
   public int getId() {
      return 331;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorFetchAutoCompleteData() {
   }

   public AssetEditorFetchAutoCompleteData(int token, @Nullable String dataset, @Nullable String query) {
      this.token = token;
      this.dataset = dataset;
      this.query = query;
   }

   public AssetEditorFetchAutoCompleteData(@Nonnull AssetEditorFetchAutoCompleteData other) {
      this.token = other.token;
      this.dataset = other.dataset;
      this.query = other.query;
   }

   @Nonnull
   public static AssetEditorFetchAutoCompleteData deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorFetchAutoCompleteData obj = new AssetEditorFetchAutoCompleteData();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 5);
         int datasetLen = VarInt.peek(buf, varPos0);
         if (datasetLen < 0) {
            throw ProtocolException.negativeLength("Dataset", datasetLen);
         }

         if (datasetLen > 4096000) {
            throw ProtocolException.stringTooLong("Dataset", datasetLen, 4096000);
         }

         obj.dataset = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 9);
         int queryLen = VarInt.peek(buf, varPos1);
         if (queryLen < 0) {
            throw ProtocolException.negativeLength("Query", queryLen);
         }

         if (queryLen > 4096000) {
            throw ProtocolException.stringTooLong("Query", queryLen, 4096000);
         }

         obj.query = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.dataset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.query != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      int datasetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int queryOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.dataset != null) {
         buf.setIntLE(datasetOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.dataset, 4096000);
      } else {
         buf.setIntLE(datasetOffsetSlot, -1);
      }

      if (this.query != null) {
         buf.setIntLE(queryOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.query, 4096000);
      } else {
         buf.setIntLE(queryOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.dataset != null) {
         size += PacketIO.stringSize(this.dataset);
      }

      if (this.query != null) {
         size += PacketIO.stringSize(this.query);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int datasetOffset = buffer.getIntLE(offset + 5);
            if (datasetOffset < 0) {
               return ValidationResult.error("Invalid offset for Dataset");
            }

            int pos = offset + 13 + datasetOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Dataset");
            }

            int datasetLen = VarInt.peek(buffer, pos);
            if (datasetLen < 0) {
               return ValidationResult.error("Invalid string length for Dataset");
            }

            if (datasetLen > 4096000) {
               return ValidationResult.error("Dataset exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += datasetLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Dataset");
            }
         }

         if ((nullBits & 2) != 0) {
            int queryOffset = buffer.getIntLE(offset + 9);
            if (queryOffset < 0) {
               return ValidationResult.error("Invalid offset for Query");
            }

            int posx = offset + 13 + queryOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Query");
            }

            int queryLen = VarInt.peek(buffer, posx);
            if (queryLen < 0) {
               return ValidationResult.error("Invalid string length for Query");
            }

            if (queryLen > 4096000) {
               return ValidationResult.error("Query exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += queryLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Query");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFetchAutoCompleteData clone() {
      AssetEditorFetchAutoCompleteData copy = new AssetEditorFetchAutoCompleteData();
      copy.token = this.token;
      copy.dataset = this.dataset;
      copy.query = this.query;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFetchAutoCompleteData other)
            ? false
            : this.token == other.token && Objects.equals(this.dataset, other.dataset) && Objects.equals(this.query, other.query);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.dataset, this.query);
   }
}
