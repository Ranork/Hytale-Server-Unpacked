package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.BlockType;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateBlockTypes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 40;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, BlockType> blockTypes;
   public boolean updateBlockTextures;
   public boolean updateModelTextures;
   public boolean updateModels;
   public boolean updateMapGeometry;

   @Override
   public int getId() {
      return 40;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockTypes() {
   }

   public UpdateBlockTypes(
      @Nonnull UpdateType type,
      int maxId,
      @Nullable Map<Integer, BlockType> blockTypes,
      boolean updateBlockTextures,
      boolean updateModelTextures,
      boolean updateModels,
      boolean updateMapGeometry
   ) {
      this.type = type;
      this.maxId = maxId;
      this.blockTypes = blockTypes;
      this.updateBlockTextures = updateBlockTextures;
      this.updateModelTextures = updateModelTextures;
      this.updateModels = updateModels;
      this.updateMapGeometry = updateMapGeometry;
   }

   public UpdateBlockTypes(@Nonnull UpdateBlockTypes other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.blockTypes = other.blockTypes;
      this.updateBlockTextures = other.updateBlockTextures;
      this.updateModelTextures = other.updateModelTextures;
      this.updateModels = other.updateModels;
      this.updateMapGeometry = other.updateMapGeometry;
   }

   @Nonnull
   public static UpdateBlockTypes deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("UpdateBlockTypes", 10, buf.readableBytes() - offset);
      } else {
         UpdateBlockTypes obj = new UpdateBlockTypes();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         obj.updateBlockTextures = buf.getByte(offset + 6) != 0;
         obj.updateModelTextures = buf.getByte(offset + 7) != 0;
         obj.updateModels = buf.getByte(offset + 8) != 0;
         obj.updateMapGeometry = buf.getByte(offset + 9) != 0;
         int pos = offset + 10;
         if ((nullBits & 1) != 0) {
            int blockTypesCount = VarInt.peek(buf, pos);
            if (blockTypesCount < 0) {
               throw ProtocolException.invalidVarInt("BlockTypes");
            }

            int blockTypesVarLen = VarInt.size(blockTypesCount);
            if (blockTypesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("BlockTypes", blockTypesCount, 4096000);
            }

            pos += blockTypesVarLen;
            obj.blockTypes = new HashMap<>(blockTypesCount);

            for (int i = 0; i < blockTypesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               BlockType val = BlockType.deserialize(buf, pos);
               pos += BlockType.computeBytesConsumed(buf, pos);
               if (obj.blockTypes.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("blockTypes", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 10;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += BlockType.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 10L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static int getMaxId(MemorySegment mem) {
      return getMaxId(mem, 0);
   }

   public static int getMaxId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   @Nullable
   public static Map<Integer, BlockType> getBlockTypes(MemorySegment mem) {
      return getBlockTypes(mem, 0);
   }

   @Nullable
   public static Map<Integer, BlockType> getBlockTypes(MemorySegment mem, int offset) {
      if (!hasBlockTypes(mem, offset)) {
         return null;
      } else {
         int off = offset + 10;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlockTypes", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockTypes", len, 4096000);
         } else {
            Map<Integer, BlockType> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               BlockType value = BlockType.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("BlockTypes", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean getUpdateBlockTextures(MemorySegment mem) {
      return getUpdateBlockTextures(mem, 0);
   }

   public static boolean getUpdateBlockTextures(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static boolean getUpdateModelTextures(MemorySegment mem) {
      return getUpdateModelTextures(mem, 0);
   }

   public static boolean getUpdateModelTextures(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7));
   }

   public static boolean getUpdateModels(MemorySegment mem) {
      return getUpdateModels(mem, 0);
   }

   public static boolean getUpdateModels(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static boolean getUpdateMapGeometry(MemorySegment mem) {
      return getUpdateMapGeometry(mem, 0);
   }

   public static boolean getUpdateMapGeometry(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   public static boolean hasBlockTypes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateBlockTypes toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateBlockTypes toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateBlockTypes", offset + 10, (int)mem.byteSize());
      } else {
         Map<Integer, BlockType> blockTypes = null;
         if (hasBlockTypes(mem, offset)) {
            int off = offset + 10;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("BlockTypes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("BlockTypes", len, 4096000);
            }

            blockTypes = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               BlockType value = BlockType.toObject(mem, off);
               off += value.computeSize();
               if (blockTypes.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("BlockTypes", key);
               }
            }
         }

         return new UpdateBlockTypes(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            blockTypes,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockTypes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      buf.writeByte(this.updateBlockTextures ? 1 : 0);
      buf.writeByte(this.updateModelTextures ? 1 : 0);
      buf.writeByte(this.updateModels ? 1 : 0);
      buf.writeByte(this.updateMapGeometry ? 1 : 0);
      if (this.blockTypes != null) {
         if (this.blockTypes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockTypes", this.blockTypes.size(), 4096000);
         }

         VarInt.write(buf, this.blockTypes.size());

         for (Entry<Integer, BlockType> e : this.blockTypes.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blockTypes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.updateBlockTextures);
      mem.set(PacketIO.PROTO_BOOL, offset + 7, this.updateModelTextures);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.updateModels);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.updateMapGeometry);
      int varOffset = offset + 10;
      if (this.blockTypes != null) {
         if (this.blockTypes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockTypes", this.blockTypes.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blockTypes.size());

         for (Entry<Integer, BlockType> e : this.blockTypes.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.blockTypes != null) {
         int blockTypesSize = 0;

         for (Entry<Integer, BlockType> kvp : this.blockTypes.entrySet()) {
            blockTypesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.blockTypes.size()) + blockTypesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 10;
            if ((nullBits & 1) != 0) {
               int blockTypesCount = VarInt.peek(buffer, v);
               if (blockTypesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for BlockTypes");
               }

               if (blockTypesCount > 4096000) {
                  return ValidationResult.error("BlockTypes exceeds max length 4096000");
               }

               v += VarInt.size(blockTypesCount);

               for (int i = 0; i < blockTypesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  v += BlockType.computeBytesConsumed(buffer, v);
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateBlockTypes clone() {
      UpdateBlockTypes copy = new UpdateBlockTypes();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.blockTypes != null) {
         Map<Integer, BlockType> m = new HashMap<>();

         for (Entry<Integer, BlockType> e : this.blockTypes.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.blockTypes = m;
      }

      copy.updateBlockTextures = this.updateBlockTextures;
      copy.updateModelTextures = this.updateModelTextures;
      copy.updateModels = this.updateModels;
      copy.updateMapGeometry = this.updateMapGeometry;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockTypes other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.maxId == other.maxId
               && Objects.equals(this.blockTypes, other.blockTypes)
               && this.updateBlockTextures == other.updateBlockTextures
               && this.updateModelTextures == other.updateModelTextures
               && this.updateModels == other.updateModels
               && this.updateMapGeometry == other.updateMapGeometry;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.blockTypes, this.updateBlockTextures, this.updateModelTextures, this.updateModels, this.updateMapGeometry);
   }
}
