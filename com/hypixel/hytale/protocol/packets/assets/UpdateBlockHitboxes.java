package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.Hitbox;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateBlockHitboxes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 41;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, Hitbox[]> blockBaseHitboxes;

   @Override
   public int getId() {
      return 41;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockHitboxes() {
   }

   public UpdateBlockHitboxes(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, Hitbox[]> blockBaseHitboxes) {
      this.type = type;
      this.maxId = maxId;
      this.blockBaseHitboxes = blockBaseHitboxes;
   }

   public UpdateBlockHitboxes(@Nonnull UpdateBlockHitboxes other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.blockBaseHitboxes = other.blockBaseHitboxes;
   }

   @Nonnull
   public static UpdateBlockHitboxes deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("UpdateBlockHitboxes", 6, buf.readableBytes() - offset);
      } else {
         UpdateBlockHitboxes obj = new UpdateBlockHitboxes();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         obj.maxId = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int blockBaseHitboxesCount = VarInt.peek(buf, pos);
            if (blockBaseHitboxesCount < 0) {
               throw ProtocolException.invalidVarInt("BlockBaseHitboxes");
            }

            int blockBaseHitboxesVarLen = VarInt.size(blockBaseHitboxesCount);
            if (blockBaseHitboxesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", blockBaseHitboxesCount, 4096000);
            }

            pos += blockBaseHitboxesVarLen;
            obj.blockBaseHitboxes = new HashMap<>(blockBaseHitboxesCount);

            for (int i = 0; i < blockBaseHitboxesCount; i++) {
               int key = buf.getIntLE(pos);
               pos += 4;
               int valLen = VarInt.peek(buf, pos);
               if (valLen < 0) {
                  throw ProtocolException.invalidVarInt("val");
               }

               int valVarLen = VarInt.size(valLen);
               if (valLen > 64) {
                  throw ProtocolException.arrayTooLong("val", valLen, 64);
               }

               if (pos + valVarLen + valLen * 24L > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("val", pos + valVarLen + valLen * 24, buf.readableBytes());
               }

               pos += valVarLen;
               Hitbox[] val = new Hitbox[valLen];

               for (int valIdx = 0; valIdx < valLen; valIdx++) {
                  val[valIdx] = Hitbox.deserialize(buf, pos);
                  pos += Hitbox.computeBytesConsumed(buf, pos);
               }

               if (obj.blockBaseHitboxes.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("blockBaseHitboxes", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            int al = VarInt.peek(buf, pos);
            pos += VarInt.size(al);

            for (int j = 0; j < al; j++) {
               pos += Hitbox.computeBytesConsumed(buf, pos);
            }
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
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
   public static Map<Integer, Hitbox[]> getBlockBaseHitboxes(MemorySegment mem) {
      return getBlockBaseHitboxes(mem, 0);
   }

   @Nullable
   public static Map<Integer, Hitbox[]> getBlockBaseHitboxes(MemorySegment mem, int offset) {
      if (!hasBlockBaseHitboxes(mem, offset)) {
         return null;
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("BlockBaseHitboxes", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", len, 4096000);
         } else {
            Map<Integer, Hitbox[]> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 24L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 24, (int)mem.byteSize());
               }

               off += valueVarLen;
               Hitbox[] value = new Hitbox[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Hitbox.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("BlockBaseHitboxes", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasBlockBaseHitboxes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateBlockHitboxes toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateBlockHitboxes toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateBlockHitboxes", offset + 6, (int)mem.byteSize());
      } else {
         Map<Integer, Hitbox[]> blockBaseHitboxes = null;
         if (hasBlockBaseHitboxes(mem, offset)) {
            int off = offset + 6;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("BlockBaseHitboxes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", len, 4096000);
            }

            blockBaseHitboxes = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               int key = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               long valuePacked = VarInt.getWithLength(mem, off);
               int valueLen = (int)valuePacked;
               int valueVarLen = (int)(valuePacked >>> 32);
               if (valueLen < 0) {
                  throw ProtocolException.negativeLength("value", valueLen);
               }

               if (valueLen > 64) {
                  throw ProtocolException.arrayTooLong("value", valueLen, 64);
               }

               if (off + valueVarLen + valueLen * 24L > mem.byteSize()) {
                  throw ProtocolException.bufferTooSmall("value", off + valueVarLen + valueLen * 24, (int)mem.byteSize());
               }

               off += valueVarLen;
               Hitbox[] value = new Hitbox[valueLen];

               for (int valueIdx = 0; valueIdx < valueLen; valueIdx++) {
                  value[valueIdx] = Hitbox.toObject(mem, off);
                  off += value[valueIdx].computeSize();
               }

               if (blockBaseHitboxes.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("BlockBaseHitboxes", key);
               }
            }
         }

         return new UpdateBlockHitboxes(
            UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), blockBaseHitboxes
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockBaseHitboxes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.blockBaseHitboxes != null) {
         if (this.blockBaseHitboxes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", this.blockBaseHitboxes.size(), 4096000);
         }

         VarInt.write(buf, this.blockBaseHitboxes.size());

         for (Entry<Integer, Hitbox[]> e : this.blockBaseHitboxes.entrySet()) {
            buf.writeIntLE(e.getKey());
            VarInt.write(buf, e.getValue().length);

            for (Hitbox arrItem : e.getValue()) {
               arrItem.serialize(buf);
            }
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.blockBaseHitboxes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.maxId);
      int varOffset = offset + 6;
      if (this.blockBaseHitboxes != null) {
         if (this.blockBaseHitboxes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockBaseHitboxes", this.blockBaseHitboxes.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.blockBaseHitboxes.size());

         for (Entry<Integer, Hitbox[]> e : this.blockBaseHitboxes.entrySet()) {
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getKey());
            varOffset += 4;
            varOffset += VarInt.set(mem, varOffset, e.getValue().length);

            for (Hitbox arrItem : e.getValue()) {
               varOffset += arrItem.serialize(mem, varOffset);
            }
         }
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.blockBaseHitboxes != null) {
         int blockBaseHitboxesSize = 0;

         for (Entry<Integer, Hitbox[]> kvp : this.blockBaseHitboxes.entrySet()) {
            blockBaseHitboxesSize += 4 + VarInt.size(kvp.getValue().length) + ((Hitbox[])kvp.getValue()).length * 24;
         }

         size += VarInt.size(this.blockBaseHitboxes.size()) + blockBaseHitboxesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            v = offset + 6;
            if ((nullBits & 1) != 0) {
               int blockBaseHitboxesCount = VarInt.peek(buffer, v);
               if (blockBaseHitboxesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for BlockBaseHitboxes");
               }

               if (blockBaseHitboxesCount > 4096000) {
                  return ValidationResult.error("BlockBaseHitboxes exceeds max length 4096000");
               }

               v += VarInt.size(blockBaseHitboxesCount);

               for (int i = 0; i < blockBaseHitboxesCount; i++) {
                  v += 4;
                  if (v > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  int valueArrCount = VarInt.peek(buffer, v);
                  if (valueArrCount < 0) {
                     return ValidationResult.error("Invalid array count for value");
                  }

                  v += VarInt.size(valueArrCount);

                  for (int valueArrIdx = 0; valueArrIdx < valueArrCount; valueArrIdx++) {
                     v += 24;
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateBlockHitboxes clone() {
      UpdateBlockHitboxes copy = new UpdateBlockHitboxes();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.blockBaseHitboxes != null) {
         Map<Integer, Hitbox[]> m = new HashMap<>();

         for (Entry<Integer, Hitbox[]> e : this.blockBaseHitboxes.entrySet()) {
            m.put(e.getKey(), Arrays.stream(e.getValue()).map(x -> x.clone()).toArray(Hitbox[]::new));
         }

         copy.blockBaseHitboxes = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateBlockHitboxes other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.blockBaseHitboxes, other.blockBaseHitboxes);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.blockBaseHitboxes);
   }
}
