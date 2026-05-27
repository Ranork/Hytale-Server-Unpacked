package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateWorldMap implements Packet, ToClientPacket {
   public static final int PACKET_ID = 241;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public MapChunk[] chunks;
   @Nullable
   public MapMarker[] addedMarkers;
   @Nullable
   public String[] removedMarkers;

   @Override
   public int getId() {
      return 241;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.WorldMap;
   }

   public UpdateWorldMap() {
   }

   public UpdateWorldMap(@Nullable MapChunk[] chunks, @Nullable MapMarker[] addedMarkers, @Nullable String[] removedMarkers) {
      this.chunks = chunks;
      this.addedMarkers = addedMarkers;
      this.removedMarkers = removedMarkers;
   }

   public UpdateWorldMap(@Nonnull UpdateWorldMap other) {
      this.chunks = other.chunks;
      this.addedMarkers = other.addedMarkers;
      this.removedMarkers = other.removedMarkers;
   }

   @Nonnull
   public static UpdateWorldMap deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("UpdateWorldMap", 13, buf.readableBytes() - offset);
      } else {
         UpdateWorldMap obj = new UpdateWorldMap();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Chunks", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int chunksCount = VarInt.peek(buf, varPos0);
            if (chunksCount < 0) {
               throw ProtocolException.invalidVarInt("Chunks");
            }

            int varIntLen = VarInt.size(chunksCount);
            if (chunksCount > 4096000) {
               throw ProtocolException.arrayTooLong("Chunks", chunksCount, 4096000);
            }

            if (varPos0 + varIntLen + chunksCount * 9L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Chunks", varPos0 + varIntLen + chunksCount * 9, buf.readableBytes());
            }

            obj.chunks = new MapChunk[chunksCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < chunksCount; i++) {
               obj.chunks[i] = MapChunk.deserialize(buf, elemPos);
               elemPos += MapChunk.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("AddedMarkers", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int addedMarkersCount = VarInt.peek(buf, varPos1);
            if (addedMarkersCount < 0) {
               throw ProtocolException.invalidVarInt("AddedMarkers");
            }

            int varIntLenx = VarInt.size(addedMarkersCount);
            if (addedMarkersCount > 4096000) {
               throw ProtocolException.arrayTooLong("AddedMarkers", addedMarkersCount, 4096000);
            }

            if (varPos1 + varIntLenx + addedMarkersCount * 38L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("AddedMarkers", varPos1 + varIntLenx + addedMarkersCount * 38, buf.readableBytes());
            }

            obj.addedMarkers = new MapMarker[addedMarkersCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < addedMarkersCount; i++) {
               obj.addedMarkers[i] = MapMarker.deserialize(buf, elemPos);
               elemPos += MapMarker.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("RemovedMarkers", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int removedMarkersCount = VarInt.peek(buf, varPos2);
            if (removedMarkersCount < 0) {
               throw ProtocolException.invalidVarInt("RemovedMarkers");
            }

            int varIntLenxx = VarInt.size(removedMarkersCount);
            if (removedMarkersCount > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedMarkers", removedMarkersCount, 4096000);
            }

            if (varPos2 + varIntLenxx + removedMarkersCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RemovedMarkers", varPos2 + varIntLenxx + removedMarkersCount * 1, buf.readableBytes());
            }

            obj.removedMarkers = new String[removedMarkersCount];
            int elemPos = varPos2 + varIntLenxx;

            for (int i = 0; i < removedMarkersCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("removedMarkers[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("removedMarkers[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("removedMarkers[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.removedMarkers[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Chunks", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += MapChunk.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("AddedMarkers", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += MapMarker.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("RemovedMarkers", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.size(sl) + sl;
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static MapChunk[] getChunks(MemorySegment mem) {
      return getChunks(mem, 0);
   }

   @Nullable
   public static MapChunk[] getChunks(MemorySegment mem, int offset) {
      if (!hasChunks(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 13, "Chunks");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Chunks", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Chunks", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Chunks", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               MapChunk[] data = new MapChunk[len];

               for (int i = 0; i < len; i++) {
                  data[i] = MapChunk.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static MapMarker[] getAddedMarkers(MemorySegment mem) {
      return getAddedMarkers(mem, 0);
   }

   @Nullable
   public static MapMarker[] getAddedMarkers(MemorySegment mem, int offset) {
      if (!hasAddedMarkers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "AddedMarkers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("AddedMarkers", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("AddedMarkers", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AddedMarkers", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               MapMarker[] data = new MapMarker[len];

               for (int i = 0; i < len; i++) {
                  data[i] = MapMarker.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String[] getRemovedMarkers(MemorySegment mem) {
      return getRemovedMarkers(mem, 0);
   }

   @Nullable
   public static String[] getRemovedMarkers(MemorySegment mem, int offset) {
      if (!hasRemovedMarkers(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "RemovedMarkers");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RemovedMarkers", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedMarkers", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedMarkers", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("RemovedMarkers", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasChunks(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasAddedMarkers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasRemovedMarkers(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UpdateWorldMap toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateWorldMap toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateWorldMap", offset + 13, (int)mem.byteSize());
      } else {
         MapChunk[] chunks = null;
         if (hasChunks(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 13, "Chunks");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Chunks", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Chunks", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Chunks", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            chunks = new MapChunk[len];

            for (int i = 0; i < len; i++) {
               chunks[i] = MapChunk.toObject(mem, off);
               off += chunks[i].computeSize();
            }
         }

         MapMarker[] addedMarkers = null;
         if (hasAddedMarkers(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 13, "AddedMarkers");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("AddedMarkers", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("AddedMarkers", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("AddedMarkers", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            addedMarkers = new MapMarker[lenx];

            for (int i = 0; i < lenx; i++) {
               addedMarkers[i] = MapMarker.toObject(mem, offx);
               offx += addedMarkers[i].computeSize();
            }
         }

         String[] removedMarkers = null;
         if (hasRemovedMarkers(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 9, 13, "RemovedMarkers");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("RemovedMarkers", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedMarkers", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedMarkers", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            removedMarkers = new String[lenxx];

            for (int i = 0; i < lenxx; i++) {
               long sp = VarInt.getWithLength(mem, offxx);
               int n = (int)sp + (int)(sp >>> 32);
               removedMarkers[i] = PacketIO.readVarString("RemovedMarkers", mem, offxx, 16384000, PacketIO.UTF8);
               offxx += n;
            }
         }

         return new UpdateWorldMap(chunks, addedMarkers, removedMarkers);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.chunks != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.addedMarkers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.removedMarkers != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int chunksOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int addedMarkersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedMarkersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.chunks != null) {
         buf.setIntLE(chunksOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.chunks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Chunks", this.chunks.length, 4096000);
         }

         VarInt.write(buf, this.chunks.length);

         for (MapChunk item : this.chunks) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(chunksOffsetSlot, -1);
      }

      if (this.addedMarkers != null) {
         buf.setIntLE(addedMarkersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.addedMarkers.length > 4096000) {
            throw ProtocolException.arrayTooLong("AddedMarkers", this.addedMarkers.length, 4096000);
         }

         VarInt.write(buf, this.addedMarkers.length);

         for (MapMarker item : this.addedMarkers) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(addedMarkersOffsetSlot, -1);
      }

      if (this.removedMarkers != null) {
         buf.setIntLE(removedMarkersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedMarkers.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedMarkers", this.removedMarkers.length, 4096000);
         }

         VarInt.write(buf, this.removedMarkers.length);

         for (String item : this.removedMarkers) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedMarkersOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.chunks != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.addedMarkers != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.removedMarkers != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.chunks != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         if (this.chunks.length > 4096000) {
            throw ProtocolException.arrayTooLong("Chunks", this.chunks.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.chunks.length);
         int chunksValueOffset = 0;

         for (int i = 0; i < this.chunks.length; i++) {
            chunksValueOffset += this.chunks[i].serialize(mem, varOffset + chunksValueOffset);
         }

         varOffset += chunksValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.addedMarkers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.addedMarkers.length > 4096000) {
            throw ProtocolException.arrayTooLong("AddedMarkers", this.addedMarkers.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.addedMarkers.length);
         int addedMarkersValueOffset = 0;

         for (int i = 0; i < this.addedMarkers.length; i++) {
            addedMarkersValueOffset += this.addedMarkers[i].serialize(mem, varOffset + addedMarkersValueOffset);
         }

         varOffset += addedMarkersValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.removedMarkers != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.removedMarkers.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedMarkers", this.removedMarkers.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removedMarkers.length);
         int removedMarkersValueOffset = 0;

         for (int i = 0; i < this.removedMarkers.length; i++) {
            removedMarkersValueOffset += PacketIO.writeVarString(mem, varOffset + removedMarkersValueOffset, this.removedMarkers[i], 16384000);
         }

         varOffset += removedMarkersValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.chunks != null) {
         int chunksSize = 0;

         for (MapChunk elem : this.chunks) {
            chunksSize += elem.computeSize();
         }

         size += VarInt.size(this.chunks.length) + chunksSize;
      }

      if (this.addedMarkers != null) {
         int addedMarkersSize = 0;

         for (MapMarker elem : this.addedMarkers) {
            addedMarkersSize += elem.computeSize();
         }

         size += VarInt.size(this.addedMarkers.length) + addedMarkersSize;
      }

      if (this.removedMarkers != null) {
         int removedMarkersSize = 0;

         for (String elem : this.removedMarkers) {
            removedMarkersSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedMarkers.length) + removedMarkersSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int chunksOffset = buffer.getIntLE(offset + 1);
            if (chunksOffset < 0 || chunksOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Chunks");
            }

            int pos = offset + 13 + chunksOffset;
            int chunksCount = VarInt.peek(buffer, pos);
            if (chunksCount < 0) {
               return ValidationResult.error("Invalid array count for Chunks");
            }

            if (chunksCount > 4096000) {
               return ValidationResult.error("Chunks exceeds max length 4096000");
            }

            pos += VarInt.size(chunksCount);

            for (int i = 0; i < chunksCount; i++) {
               ValidationResult structResult = MapChunk.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid MapChunk in Chunks[" + i + "]: " + structResult.error());
               }

               pos += MapChunk.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int addedMarkersOffset = buffer.getIntLE(offset + 5);
            if (addedMarkersOffset < 0 || addedMarkersOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for AddedMarkers");
            }

            int posx = offset + 13 + addedMarkersOffset;
            int addedMarkersCount = VarInt.peek(buffer, posx);
            if (addedMarkersCount < 0) {
               return ValidationResult.error("Invalid array count for AddedMarkers");
            }

            if (addedMarkersCount > 4096000) {
               return ValidationResult.error("AddedMarkers exceeds max length 4096000");
            }

            posx += VarInt.size(addedMarkersCount);

            for (int i = 0; i < addedMarkersCount; i++) {
               ValidationResult structResult = MapMarker.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid MapMarker in AddedMarkers[" + i + "]: " + structResult.error());
               }

               posx += MapMarker.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int removedMarkersOffset = buffer.getIntLE(offset + 9);
            if (removedMarkersOffset < 0 || removedMarkersOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for RemovedMarkers");
            }

            int posxx = offset + 13 + removedMarkersOffset;
            int removedMarkersCount = VarInt.peek(buffer, posxx);
            if (removedMarkersCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedMarkers");
            }

            if (removedMarkersCount > 4096000) {
               return ValidationResult.error("RemovedMarkers exceeds max length 4096000");
            }

            posxx += VarInt.size(removedMarkersCount);

            for (int i = 0; i < removedMarkersCount; i++) {
               int strLen = VarInt.peek(buffer, posxx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedMarkers");
               }

               posxx += VarInt.size(strLen);
               posxx += strLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in RemovedMarkers");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateWorldMap clone() {
      UpdateWorldMap copy = new UpdateWorldMap();
      copy.chunks = this.chunks != null ? Arrays.stream(this.chunks).map(e -> e.clone()).toArray(MapChunk[]::new) : null;
      copy.addedMarkers = this.addedMarkers != null ? Arrays.stream(this.addedMarkers).map(e -> e.clone()).toArray(MapMarker[]::new) : null;
      copy.removedMarkers = this.removedMarkers != null ? Arrays.copyOf(this.removedMarkers, this.removedMarkers.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateWorldMap other)
            ? false
            : Arrays.equals((Object[])this.chunks, (Object[])other.chunks)
               && Arrays.equals((Object[])this.addedMarkers, (Object[])other.addedMarkers)
               && Arrays.equals((Object[])this.removedMarkers, (Object[])other.removedMarkers);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.chunks);
      result = 31 * result + Arrays.hashCode((Object[])this.addedMarkers);
      return 31 * result + Arrays.hashCode((Object[])this.removedMarkers);
   }
}
