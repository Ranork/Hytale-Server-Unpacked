package com.hypixel.hytale.protocol.packets.worldmap;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      UpdateWorldMap obj = new UpdateWorldMap();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int chunksCount = VarInt.peek(buf, varPos0);
         if (chunksCount < 0) {
            throw ProtocolException.negativeLength("Chunks", chunksCount);
         }

         if (chunksCount > 4096000) {
            throw ProtocolException.arrayTooLong("Chunks", chunksCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
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
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int addedMarkersCount = VarInt.peek(buf, varPos1);
         if (addedMarkersCount < 0) {
            throw ProtocolException.negativeLength("AddedMarkers", addedMarkersCount);
         }

         if (addedMarkersCount > 4096000) {
            throw ProtocolException.arrayTooLong("AddedMarkers", addedMarkersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + addedMarkersCount * 38L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("AddedMarkers", varPos1 + varIntLen + addedMarkersCount * 38, buf.readableBytes());
         }

         obj.addedMarkers = new MapMarker[addedMarkersCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < addedMarkersCount; i++) {
            obj.addedMarkers[i] = MapMarker.deserialize(buf, elemPos);
            elemPos += MapMarker.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int removedMarkersCount = VarInt.peek(buf, varPos2);
         if (removedMarkersCount < 0) {
            throw ProtocolException.negativeLength("RemovedMarkers", removedMarkersCount);
         }

         if (removedMarkersCount > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedMarkers", removedMarkersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + removedMarkersCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("RemovedMarkers", varPos2 + varIntLen + removedMarkersCount * 1, buf.readableBytes());
         }

         obj.removedMarkers = new String[removedMarkersCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < removedMarkersCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("removedMarkers[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("removedMarkers[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.removedMarkers[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            pos0 += MapChunk.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += MapMarker.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.length(buf, pos2) + sl;
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
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
            if (chunksOffset < 0) {
               return ValidationResult.error("Invalid offset for Chunks");
            }

            int pos = offset + 13 + chunksOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Chunks");
            }

            int chunksCount = VarInt.peek(buffer, pos);
            if (chunksCount < 0) {
               return ValidationResult.error("Invalid array count for Chunks");
            }

            if (chunksCount > 4096000) {
               return ValidationResult.error("Chunks exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

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
            if (addedMarkersOffset < 0) {
               return ValidationResult.error("Invalid offset for AddedMarkers");
            }

            int posx = offset + 13 + addedMarkersOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AddedMarkers");
            }

            int addedMarkersCount = VarInt.peek(buffer, posx);
            if (addedMarkersCount < 0) {
               return ValidationResult.error("Invalid array count for AddedMarkers");
            }

            if (addedMarkersCount > 4096000) {
               return ValidationResult.error("AddedMarkers exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

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
            if (removedMarkersOffset < 0) {
               return ValidationResult.error("Invalid offset for RemovedMarkers");
            }

            int posxx = offset + 13 + removedMarkersOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RemovedMarkers");
            }

            int removedMarkersCount = VarInt.peek(buffer, posxx);
            if (removedMarkersCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedMarkers");
            }

            if (removedMarkersCount > 4096000) {
               return ValidationResult.error("RemovedMarkers exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < removedMarkersCount; i++) {
               int strLen = VarInt.peek(buffer, posxx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedMarkers");
               }

               posxx += VarInt.length(buffer, posxx);
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
