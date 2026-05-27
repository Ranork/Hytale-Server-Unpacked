package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchAutoCompleteData", 13, buf.readableBytes() - offset);
      } else {
         AssetEditorFetchAutoCompleteData obj = new AssetEditorFetchAutoCompleteData();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Dataset", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int datasetLen = VarInt.peek(buf, varPos0);
            if (datasetLen < 0) {
               throw ProtocolException.invalidVarInt("Dataset");
            }

            int datasetVarIntLen = VarInt.size(datasetLen);
            if (datasetLen > 4096000) {
               throw ProtocolException.stringTooLong("Dataset", datasetLen, 4096000);
            }

            if (varPos0 + datasetVarIntLen + datasetLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Dataset", varPos0 + datasetVarIntLen + datasetLen, buf.readableBytes());
            }

            obj.dataset = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Query", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int queryLen = VarInt.peek(buf, varPos1);
            if (queryLen < 0) {
               throw ProtocolException.invalidVarInt("Query");
            }

            int queryVarIntLen = VarInt.size(queryLen);
            if (queryLen > 4096000) {
               throw ProtocolException.stringTooLong("Query", queryLen, 4096000);
            }

            if (varPos1 + queryVarIntLen + queryLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Query", varPos1 + queryVarIntLen + queryLen, buf.readableBytes());
            }

            obj.query = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Dataset", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Query", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getDataset(MemorySegment mem) {
      return getDataset(mem, 0);
   }

   @Nullable
   public static String getDataset(MemorySegment mem, int offset) {
      return hasDataset(mem, offset)
         ? PacketIO.readVarString("Dataset", mem, offset + getValidatedOffset(mem, offset, 5, 13, "Dataset"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getQuery(MemorySegment mem) {
      return getQuery(mem, 0);
   }

   @Nullable
   public static String getQuery(MemorySegment mem, int offset) {
      return hasQuery(mem, offset)
         ? PacketIO.readVarString("Query", mem, offset + getValidatedOffset(mem, offset, 9, 13, "Query"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasDataset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasQuery(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetEditorFetchAutoCompleteData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorFetchAutoCompleteData toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorFetchAutoCompleteData", offset + 13, (int)mem.byteSize());
      } else {
         return new AssetEditorFetchAutoCompleteData(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasDataset(mem, offset)
               ? PacketIO.readVarString("Dataset", mem, offset + getValidatedOffset(mem, offset, 5, 13, "Dataset"), 4096000, PacketIO.UTF8)
               : null,
            hasQuery(mem, offset)
               ? PacketIO.readVarString("Query", mem, offset + getValidatedOffset(mem, offset, 9, 13, "Query"), 4096000, PacketIO.UTF8)
               : null
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.dataset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.query != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 13;
      if (this.dataset != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.dataset, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.query != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.query, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
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
            if (datasetOffset < 0 || datasetOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Dataset");
            }

            int pos = offset + 13 + datasetOffset;
            int datasetLen = VarInt.peek(buffer, pos);
            if (datasetLen < 0) {
               return ValidationResult.error("Invalid string length for Dataset");
            }

            if (datasetLen > 4096000) {
               return ValidationResult.error("Dataset exceeds max length 4096000");
            }

            pos += VarInt.size(datasetLen);
            pos += datasetLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Dataset");
            }
         }

         if ((nullBits & 2) != 0) {
            int queryOffset = buffer.getIntLE(offset + 9);
            if (queryOffset < 0 || queryOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Query");
            }

            int posx = offset + 13 + queryOffset;
            int queryLen = VarInt.peek(buffer, posx);
            if (queryLen < 0) {
               return ValidationResult.error("Invalid string length for Query");
            }

            if (queryLen > 4096000) {
               return ValidationResult.error("Query exceeds max length 4096000");
            }

            posx += VarInt.size(queryLen);
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
